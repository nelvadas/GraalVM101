package com.oracle.graalvm.demo;


import io.micronaut.http.annotation.*;

import java.util.stream.Stream;

@Controller("/price")
public class PriceController {

	@Get("/")
	public String index() {
		return "Welcome to GraalVM Spring Boot HOL! With Micronaut\n";
	}

	/**
	 * @param name : Bond name
	 * @param interestRate  : Contractual Interest Rate
	 * @param principal : the bond PAR/FACE Value or principal ( amount returned to the bond acquirer at the maturity)
	 * @param maturity : Number of years/coupon for the bond to mature: For simplicity we stick on annual payments
	 * @param yield : Market Interest rate / appropriate yield to maturity
	 * @see  <a href="https://en.wikipedia.org/wiki/Bond_valuation#Present_value_approach"></a>
	 * @return The Bond Selling price
	 *
	 * curl 'localhost:8080/price/graalvm/100/10/?yield=0.03&interestRate=0.02'
	 * 91,470
	 */
	@Get("/{name}/{principal}/{maturity}")
	String price (
			@PathVariable(name = "name") String name,
			@PathVariable(value ="principal" ) double principal,
			@PathVariable(value = "maturity") int maturity,
			@QueryValue(value = "yield") double yield,
			@QueryValue( value="interestRate" ) double interestRate
	)

	{

		Double bondMarketPrice=
				Stream.iterate(1, year -> year +1)
				.limit(maturity)
				.parallel()
				.mapToDouble(t -> interestRate*principal/Math.pow(1+yield,t)) // compute coupon (interest) stream
				.sum() ;

		bondMarketPrice += principal/Math.pow(1+yield,maturity);

		return String.format("%.3f\n",bondMarketPrice);
	}

}
