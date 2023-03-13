package com.zerobase.stockdividendproject.web;

import com.zerobase.stockdividendproject.model.Company;
import com.zerobase.stockdividendproject.persist.entity.CompanyEntity;
import com.zerobase.stockdividendproject.service.CompanyService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {


	private final CompanyService companyService;

	@GetMapping("/autocomplete")
	public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
		var result = this.companyService.autoComplete(keyword);
		return ResponseEntity.ok(result);
	}

	@GetMapping
	public ResponseEntity<?> searchCompany(final Pageable pageable) {
		Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
		return ResponseEntity.ok(companies);
	}

	@PostMapping
	public ResponseEntity<?> addCompany(@RequestBody Company request) {
		String ticker = request.getTicker().trim();
		if (ObjectUtils.isEmpty(ticker)){
			throw new RuntimeException("ticker is empty");
		}

		Company company = this.companyService.save(ticker);
		this.companyService.addAutocompleteKeyword(company.getName());
		return ResponseEntity.ok(company);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteCompany() {
		return null;
	}
}
