package com.zerobase.stockdividendproject.service;

import com.zerobase.stockdividendproject.exception.impl.NoCompanyException;
import com.zerobase.stockdividendproject.model.Company;
import com.zerobase.stockdividendproject.model.Dividend;
import com.zerobase.stockdividendproject.model.ScrapedResult;
import com.zerobase.stockdividendproject.model.constants.CacheKey;
import com.zerobase.stockdividendproject.persist.CompanyRepository;
import com.zerobase.stockdividendproject.persist.DividendRepository;
import com.zerobase.stockdividendproject.persist.entity.CompanyEntity;
import com.zerobase.stockdividendproject.persist.entity.DividendEntity;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	// 요청이 얼마나 자주 들어오는가? <- 요청이 잦을수록 캐쉬 필요 o
	// 자주 변경되는 데이터 인가? <- 바뀔 가능성이 많을수록 캐쉬 필요 x
	@Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
	public ScrapedResult getDividendByCompanyName(String companyName) {
		log.info("search company -> " + companyName);
		// 1. 회사명을 기준으로 회사 정보를 조회
		CompanyEntity companyEntity = this.companyRepository
			.findByName(companyName)
			.orElseThrow(
				() -> new NoCompanyException()
		);
		// 2. 조회된 회사 ID 로 배당금 정보 조회
		List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(companyEntity.getId());

		// 3. 결과 조합 후 반환
		List<Dividend> dividends =
			dividendEntities.stream()
				.map(e -> new Dividend(e.getDate(), e.getDividend()))
				.collect(Collectors.toList());

		return new ScrapedResult(new Company(companyEntity.getTicker(), companyEntity.getName())
								,dividends);
	}
}
