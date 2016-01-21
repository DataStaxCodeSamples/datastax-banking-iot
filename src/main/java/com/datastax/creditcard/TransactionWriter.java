package com.datastax.creditcard;

import java.util.concurrent.BlockingQueue;

import com.datastax.creditcard.dao.CreditCardDao;
import com.datastax.creditcard.model.Transaction;
import com.datastax.demo.utils.KillableRunner;

class TransactionWriter implements KillableRunner {

	private volatile boolean shutdown = false;
	private CreditCardDao dao;
	private BlockingQueue<Transaction> queue;

	public TransactionWriter(CreditCardDao dao, BlockingQueue<Transaction> queue) {
		this.dao = dao;
		this.queue = queue;
	}

	@Override
	public void run() {
		Transaction transaction;
		while(!shutdown){				
			transaction = queue.poll(); 
			
			if (transaction!=null){
				try {
					this.dao.insertTransactionAsync(transaction);
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
