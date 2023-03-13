package com.zerobase.stockdividendproject.service;

import com.zerobase.stockdividendproject.model.Company;
import com.zerobase.stockdividendproject.model.ScrapedResult;
import com.zerobase.stockdividendproject.persist.CompanyRepository;
import com.zerobase.stockdividendproject.persist.DividendRepository;
import com.zerobase.stockdividendproject.persist.entity.CompanyEntity;
import com.zerobase.stockdividendproject.persist.entity.DividendEntity;
import com.zerobase.stockdividendproject.scraper.Scraper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@AllArgsConstructor
public class CompanyService {

	private final Trie trie;
	private final Scraper yahooFinanceScraper;

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;


	public Company save(String ticker) {
		boolean exists = this.companyRepository.existsByTicker(ticker);
		if (exists){
			throw new RuntimeException("already exists ticker -> " + ticker);
		}
		return this.storeCompanyAndDividend(ticker);
	}

	public Page<CompanyEntity> getAllCompany(Pageable pageable) {
		return this.companyRepository.findAll(pageable);
	}
	private Company storeCompanyAndDividend(String ticker) {
		// ticker 를 기준으로 회사를 스크래핑
		Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
		if (ObjectUtils.isEmpty(company)){
			throw new RuntimeException("failed to scrap ticker -> " + ticker);
		}

		// 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
		ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

		// 스크래핑 결과
		CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
		List<DividendEntity> dividendEntities =
									scrapedResult.getDividendEntities().stream()
										.map(e -> new DividendEntity(companyEntity.getId(), e))
										.collect(Collectors.toList());

		this.dividendRepository.saveAll(dividendEntities);
		return company;
	}

	public void addAutocompleteKeyword(String keyword) {
		this.trie.put(keyword, null);
	}

	public List<String> autoComplete(String keyword) {
		return (List<String>) this.trie.prefixMap(keyword).keySet()
				.stream().collect(Collectors.toList());
	}

	public void deleteAutoCompleteKeyword(String keyword) {
		this.trie.remove(keyword);
	}
}
