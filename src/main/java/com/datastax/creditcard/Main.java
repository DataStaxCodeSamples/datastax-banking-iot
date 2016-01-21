package com.datastax.creditcard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.creditcard.dao.CreditCardDao;
import com.datastax.creditcard.model.Transaction;
import com.datastax.demo.utils.KillableRunner;
import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.ThreadUtils;
import com.datastax.demo.utils.Timer;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);
	private DateTime date;
	private static int BATCH = 10000;
	private boolean insert = false;

	public Main() {

		// Create test date at midnight
		this.date = new DateTime().minusDays(100).withTimeAtStartOfDay();

		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
		String noOfCreditCardsStr = PropertyHelper.getProperty("noOfCreditCards", "10000");
		String noOfTransactionsStr = PropertyHelper.getProperty("noOfTransactions", "100000");

		BlockingQueue<Transaction> queue = new ArrayBlockingQueue<Transaction>(1000);
		List<KillableRunner> tasks = new ArrayList<>();
		
		//Executor for Threads
		int noOfThreads = Integer.parseInt(PropertyHelper.getProperty("noOfThreads", "8"));
		ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
		CreditCardDao dao = new CreditCardDao(contactPointsStr.split(","));

		int noOfTransactions = Integer.parseInt(noOfTransactionsStr);
		int noOfCreditCards = Integer.parseInt(noOfCreditCardsStr);

		logger.info("Writing " + noOfTransactions + " transactions for " + noOfCreditCards + " credit cards.");

		for (int i = 0; i < noOfThreads; i++) {
			
			KillableRunner task = new TransactionWriter(dao, queue);
			executor.execute(task);
			tasks.add(task);
		}
		
		Timer timer = new Timer();
		for (int i = 0; i < noOfTransactions; i++) {
			
			try{
				queue.put(createRandomTransaction(noOfCreditCards));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		timer.end();
		ThreadUtils.shutdown(tasks, executor);
		System.exit(0);
	}
	
	/**
	 * Creates a random transaction with some skew for some accounts.
	 * @param noOfCreditCards
	 * @return
	 */
	private Transaction createRandomTransaction(int noOfCreditCards) {
		
		int creditCardNo = new Double(Math.ceil(Math.random() * noOfCreditCards)).intValue();
		
		//Allow for some skew
		if (Math.random() < .05) creditCardNo = creditCardNo % 1000;
		
		int noOfItems = new Double(Math.ceil(Math.random() * 5)).intValue();

		String location = locations.get(new Double(Math.random() * locations.size()).intValue());

		int randomLocation = new Double(Math.random() * issuers.size()).intValue();
		String issuer = issuers.get(randomLocation);
		String note = notes.get(randomLocation);
		String tag = tagList.get(randomLocation);
		Set<String> tags = new HashSet<String>();
		tags.add(note);
		tags.add(tag);

		// create time by adding a random no of seconds to the midnight of
		// yesterday.
		date = date.plusMillis(new Double(Math.random() * 200).intValue());

		Transaction transaction = new Transaction();
		createItemsAndAmount(noOfItems, transaction);
		transaction.setCreditCardNo(new Integer(creditCardNo).toString());
		transaction.setMerchant(issuer);
		transaction.setTransactionId(UUID.randomUUID().toString());
		transaction.setTransactionTime(date.toDate());
		transaction.setLocation(location);
		transaction.setNotes(note);
		transaction.setTags(tags);
		return transaction;
	}

	private void createItemsAndAmount(int noOfItems, Transaction transaction) {
		Map<String, Double> items = new HashMap<String, Double>();
		double totalAmount = 0;

		for (int i = 0; i < noOfItems; i++) {

			double amount = new Double(Math.random() * 1000);
			items.put("item" + i, amount);

			totalAmount += amount;
		}
		transaction.setAmount(totalAmount);
		transaction.setItems(items);		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();

		System.exit(0);
	}

	public static List<String> locations = Arrays.asList("London", "Manchester", "Liverpool", "Glasgow", "Dundee",
			"Birmingham");

	public static List<String> issuers = Arrays.asList("Tesco", "Sainsbury", "Asda Wal-Mart Stores", "Morrisons",
			"Marks & Spencer", "Boots", "John Lewis", "Waitrose", "Argos", "Co-op", "Currys", "PC World", "B&Q",
			"Somerfield", "Next", "Spar", "Amazon", "Costa", "Starbucks", "BestBuy", "Wickes", "TFL", "National Rail",
			"Pizza Hut", "Local Pub");

	public static List<String> notes = Arrays.asList("Shopping", "Shopping", "Shopping", "Shopping", "Shopping", "Pharmacy",
			"HouseHold", "Shopping", "Household", "Shopping", "Tech", "Tech", "Diy", "Shopping", "Clothes", "Shopping",
			"Amazon", "Coffee", "Coffee", "Tech", "Diy", "Travel", "Travel", "Eating out", "Eating out");
	
	public static List<String> tagList = Arrays.asList("Home", "Home", "Home", "Home", "Home", "Home",
			"Home", "Home", "Work", "Work", "Work", "Home", "Home", "Home", "Work", "Work",
			"Home", "Work", "Work", "Work", "Work", "Work", "Work", "Work", "Work");

}
