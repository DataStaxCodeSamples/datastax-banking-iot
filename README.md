Transaction Search Demo
========================

This requires DataStax Enterprise running in Solr mode.

This demo shows 3 ways to search transactions in a users account. In this demo we will presume that users can enter notes for each transaction on their account and then they will want to search by some or all of these notes.

To create the schema, run the following

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup"
	
To create some transactions, run the following 
	
	mvn clean compile exec:java -Dexec.mainClass="com.datastax.creditcard.Main" 

You can the following to change the default no of transactions and credit cards 
	
	-DnoOfTransactions=10000000 -DnoOfCreditCards=1000000
	
To create the solr core, run 

	bin/dsetool create_core datastax_transaction_search_demo.latest_transactions generateResources=true reindex=true coreOptions=rt.yaml

An example of cql queries would be

For the latest transaction table we can run the following types of queries
```
use datastax_banking_iot;

select * from latest_transactions where cc_no = '1234123412341234';

select * from latest_transactions where cc_no = '1234123412341234' and transaction_time > '2015-12-31';

select * from latest_transactions where cc_no = '1234123412341234' and transaction_time > '2015-12-31' and transaction_time < '2016-01-27';
```
For the (historic) transaction table we need to add the year into our queries.

```
select * from transactions where cc_no = '1234123412341234' and year=2016;

select * from transactions where cc_no = '1234123412341234' and year = 2016 and transaction_time > '2015-12-31';

select * from transactions where cc_no = '1234123412341234' and year = 2016 and transaction_time > '2015-12-31' and transaction_time < '2016-01-27';
```
Using the solr_query

Get all the latest transactions from PC World in London (This is accross all credit cards and users)
```
select * from latest_transactions where solr_query = 'merchant:PC+World location:London' limit  100;
```
Get all the latest transactions for credit card '1' that have a tag of Work. 
```
select * from latest_transactions where solr_query = '{"q":"cc_no:1", "fq":"tags:Work"}' limit  1000;
```
Gell all the transaction for credit card '1' that have a tag of Work and are within the last month
```
select * from latest_transactions where solr_query = '{"q":"cc_no:1", "fq":"tags:Work", "fq":"transaction_time:[NOW-30DAY TO *]"}' limit  1000;
```

	
To run the requests run the following 
	
	mvn clean compile exec:java -Dexec.mainClass="com.datastax.creditcard.RunRequests"

To change the no of requests add the following

	-DnoOfRequests=100000 -DnoOfCreditCards=1000000	
	

	
To remove the tables and the schema, run the following.

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaTeardown"
    
    
