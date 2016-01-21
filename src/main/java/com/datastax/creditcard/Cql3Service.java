package com.datastax.creditcard;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.creditcard.dao.CreditCardDao;
import com.datastax.creditcard.model.Transaction;
import com.datastax.demo.utils.Timer;

public class Cql3Service implements SearchService{
	
	private static Logger logger = LoggerFactory.getLogger(Cql3Service.class);
	private CreditCardDao dao;
	private long timerSum = 0;
	private AtomicLong timerCount= new AtomicLong();

	public Cql3Service (CreditCardDao dao){
		this.dao = dao;
	}

	@Override
	public List<Transaction> getTransactionsByTagAndDate(String ccNo, Set<String> search, DateTime from, DateTime to) {
		Timer timer = new Timer();
		List<Transaction> transactions = dao.getLatestTransactionsForCCNoTagsAndDate(ccNo, search, from, to);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}
	
	@Override
	public double getTimerAvg(){
		return timerSum/timerCount.get();
	}
}
