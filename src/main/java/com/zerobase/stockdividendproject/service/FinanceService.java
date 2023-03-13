package com.zerobase.stockdividendproject.service;

import com.zerobase.stockdividendproject.model.Company;
import com.zerobase.stockdividendproject.model.Dividend;
import com.zerobase.stockdividendproject.model.ScrapedResult;
import com.zerobase.stockdividendproject.persist.CompanyRepository;
import com.zerobase.stockdividendproject.persist.DividendRepository;
import com.zerobase.stockdividendproject.persist.entity.CompanyEntity;
import com.zerobase.stockdividendproject.persist.entity.DividendEntity;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FinanceService {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;
	public ScrapedResult getDividendByCompanyName(String companyName) {

		// 1. 회사명을 기준으로 회사 정보를 조회
		CompanyEntity companyEntity = this.companyRepository
			.findByName(companyName)
			.orElseThrow(
				() -> new RuntimeException("존재하지 않는 회사입니다.")
		);
		// 2. 조회된 회사 ID 로 배당금 정보 조회
		List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(companyEntity.getId());

		// 3. 결과 조합 후 반환
		List<Dividend> dividends = dividendEntities.stream().map(e -> Dividend.builder()
			.dividend(e.getDividend())
			.date(e.getDate())
			.build()).collect(Collectors.toList());

		return new ScrapedResult(Company.builder()
									.ticker(companyEntity.getTicker())
									.name(companyEntity.getName())
									.build(),
								dividends);
	}
}
