package com.datastax.banking.service;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.datastax.banking.model.Transaction;

public interface SearchService {

	public double getTimerAvg();

	List<Transaction> getTransactionsByTagAndDate(String ccNo, Set<String> search, DateTime from, DateTime to);
}
