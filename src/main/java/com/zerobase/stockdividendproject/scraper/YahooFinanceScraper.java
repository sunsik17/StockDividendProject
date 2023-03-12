package com.zerobase.stockdividendproject.scraper;

import com.zerobase.stockdividendproject.model.Company;
import com.zerobase.stockdividendproject.model.ScrapedResult;
import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class YahooFinanceScraper {

	private static final String URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&&period2=%d&interval=1mo";
	public ScrapedResult scrap(Company company) {
		try {
			Connection connection = Jsoup.connect(URL);
			Document document = connection.get();

			Elements elements = document.getElementsByAttributeValue("data-test", "history-prices");
			Element element = elements.get(0);

			Element tbody = element.children().get(1);
			for (Element e : tbody.children()) {
				String txt = e.text();
				if (!txt.endsWith("Dividend")) {
					continue;
				}

				String[] splits = txt.split("");
				String month = splits[0];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
