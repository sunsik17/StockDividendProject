package com.zerobase.stockdividendproject.scheduler;

import com.zerobase.stockdividendproject.model.Company;
import com.zerobase.stockdividendproject.model.ScrapedResult;
import com.zerobase.stockdividendproject.persist.CompanyRepository;
import com.zerobase.stockdividendproject.persist.DividendRepository;
import com.zerobase.stockdividendproject.persist.entity.CompanyEntity;
import com.zerobase.stockdividendproject.persist.entity.DividendEntity;
import com.zerobase.stockdividendproject.scraper.Scraper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	private final Scraper yahooFinanceScraper;


	//일정주기마다 수행
	@Scheduled(cron = "${scheduler.scrap.yahoo}")
	public void yahooFinanceScheduling() {
		log.info("scraping scheduler is started");
		// 저장된 회사 목록을 조회
		List<CompanyEntity> companies = this.companyRepository.findAll();

		// 회사마다 배당금 정보를 새로 스크래핑
		for (var company : companies) {
			log.info("scraping scheduler is started -> " + company.getName());
			ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(Company.builder()
				.name(company.getName())
				.ticker(company.getTicker())
				.build());

			// 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
			scrapedResult.getDividendEntities().stream()
				// 디비든 모델을 디비든 엔티티로 매핑
				.map(e -> new DividendEntity(company.getId(), e))
				// 엘리먼트를 하나씯 디비든 레파지토리에 삽입
				.forEach(e -> {
					boolean exists = this.dividendRepository.existsByCompanyIdAndDate(
						e.getCompanyId(), e.getDate());
					if (!exists) {
						this.dividendRepository.save(e);
					}
				});

			// 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
			try {
				Thread.sleep(3000); // 3 second;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
