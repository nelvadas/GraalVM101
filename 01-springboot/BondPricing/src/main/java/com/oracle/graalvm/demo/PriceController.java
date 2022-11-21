package com.oracle.graalvm.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@RestController
public class PriceController {

	@GetMapping("/")
	public String index() {
		return "Welcome to GraalVM Spring Boot HOL!\n";
	}

	/**
	 * @param name : Bond name
	 * @param interestRate  : Contractual Interest Rate
	 * @param principal : the bond PAR/FACE Value or principal ( amount returned to the bond acquirer at the maturity)
	 * @param maturity : Number of years/coupon for the bond to mature: For simplicity we stick on annual payments
	 * @param yield : Market Interest rate / appropriate yield to maturity
	 * @see  <a href="https://en.wikipedia.org/wiki/Bond_valuation#Present_value_approach"></a>
	 * @return The Bond Selling price
	 */
	@GetMapping("/price/{name}/{principal}/{maturity}")
	ResponseEntity price (
			@PathVariable(name = "name") String name,
			@PathVariable(value ="principal" , required = true ) double principal,
			@PathVariable(value = "maturity", required = true) int maturity,
			@RequestParam(value = "yield", required = true ) double yield,
			@RequestParam( name="interestRate",required = true ) double interestRate
	)

	{

		Double bondMarketPrice=
				Stream.iterate(1, year -> year +1)
				.limit(maturity)
				.parallel()
				.mapToDouble(t -> interestRate*principal/Math.pow(1+yield,t)) // compute coupon (interest) stream
				.sum() ;

		bondMarketPrice += principal/Math.pow(1+yield,maturity);

		return ResponseEntity.ok().body(String.format("%.3f\n",bondMarketPrice));
	}

}
