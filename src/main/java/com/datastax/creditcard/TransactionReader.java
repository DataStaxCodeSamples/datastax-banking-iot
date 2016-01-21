package com.datastax.creditcard;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.creditcard.model.Transaction;
import com.datastax.demo.utils.KillableRunner;

class TransactionReader implements KillableRunner {

	private static Logger logger = LoggerFactory.getLogger(TransactionReader.class);
	private volatile boolean shutdown = false;
	private SearchService service;
	private BlockingQueue<Transaction> queue;

	public TransactionReader(SearchService service, BlockingQueue<Transaction> queue) {
		this.service = service;
		this.queue = queue;
	}

	@Override
	public void run() {
		Transaction transaction;
		while(!shutdown){				
			transaction = queue.poll(); 
			
			if (transaction!=null){
				try {
					List<Transaction> latestTransactions = this.service.getTransactionsByTagAndDate(transaction.getCreditCardNo(), 
							transaction.getTags(), DateTime.now().minusDays(100), DateTime.now());
					logger.info(latestTransactions.size() + "");
					
					if (latestTransactions.size() == 10){
						logger.info(transaction.getNotes() + " - " + latestTransactions.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}				
		}				
	}
	
	@Override
    public void shutdown() {
        shutdown = true;
    }
}