package com.zerobase.stockdividendproject.scraper;

import com.zerobase.stockdividendproject.model.Company;
import com.zerobase.stockdividendproject.model.Dividend;
import com.zerobase.stockdividendproject.model.ScrapedResult;
import com.zerobase.stockdividendproject.model.constants.Month;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class YahooFinanceScraper implements Scraper {

	private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
	private static final String SUMMARY_URL = "http://finance.yahoo.com/quote/%s?p=%s";
	private static final long START_TIME = 86400;

	@Override
	public ScrapedResult scrap(Company company) {
		var scrapedResult = new ScrapedResult();
		scrapedResult.setCompany(company);

		try {
			long now = System.currentTimeMillis() / 1000;

			String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
			Connection connection = Jsoup.connect(url);
			Document document = connection.get();

			Elements parsingDivs = document.getElementsByAttributeValue("data-test",
				"historical-prices");
			Element table = parsingDivs.get(0);

			Element tbody = table.children().get(1);

			List<Dividend> dividends = new ArrayList<>();
			for (Element e : tbody.children()) {
				String txt = e.text();
				if (!txt.endsWith("Dividend")) {
					continue;
				}

				String[] splits = txt.split(" ");
				int month = Month.strToNumber(splits[0]);
				int day = Integer.parseInt(splits[1].replace(",", ""));
				int year = Integer.parseInt(splits[2]);
				String dividend = splits[3];

				if (month < 0) {
					throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
				}

				dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

			}
			scrapedResult.setDividendEntities(dividends);
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		return scrapedResult;
	}

	@Override
	public Company scrapCompanyByTicker(String ticker) {

		String url = String.format(SUMMARY_URL, ticker, ticker);

		try {
			Document document = Jsoup.connect(url).get();
			Element titleElement = document.getElementsByTag("h1").get(0);
			String title = titleElement.text().split(" - ")[1].trim();

			return new Company(ticker, title);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
