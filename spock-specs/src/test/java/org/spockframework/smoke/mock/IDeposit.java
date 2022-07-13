/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock;

/**
 * A bank deposit with <a href="https://en.wikipedia.org/wiki/Compound_interest">compound interest</a>.
 */
public interface IDeposit {
  class DepositException extends Exception {
    public DepositException(String message) {
      super(message);
    }
  }

  double getAnnualNominalInterestRate();
  double getCompoundingPeriodInMonths();

  default double getCompoundingPeriodsPerYear() {
    return 12 / getCompoundingPeriodInMonths();
  }

  default double getBalance(double principalAmount, double numberOfYears) throws DepositException {
    if(principalAmount < 0) throw new IllegalArgumentException("Invalid principalAmount: " + principalAmount);
    if(numberOfYears < 0) throw new DepositException("Invalid numberOfYears: " + numberOfYears);
    double j = getAnnualNominalInterestRate();
    double n = getCompoundingPeriodsPerYear();
    return principalAmount * Math.pow(1 + j / n, n * numberOfYears);
  }

  default double getCompoundInterest(double principalAmount, double numberOfYears) throws DepositException {
    return getBalance(principalAmount, numberOfYears) - principalAmount;
  }
}
