package com.datastax.banking;

import java.util.concurrent.BlockingQueue;

import org.joda.time.DateTime;

import com.datastax.banking.model.Transaction;
import com.datastax.banking.service.SearchService;
import com.datastax.demo.utils.KillableRunner;

class TransactionReader implements KillableRunner {

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
					this.service.getTransactionsByTagAndDate(transaction.getCreditCardNo(), 
							transaction.getTags(), DateTime.now().minusDays(100), DateTime.now());
					
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