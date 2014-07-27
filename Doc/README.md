Automatic Gathering of Educational Digital Resources 
=========
This work was made for my bachelor's degree thesis in Computer Science at Universidad Nacional de Rosario.

Overview
--
To populate Institutional Repositories  with Text Learning Objects (TLO), 
it is important to develop tools to detect all educational digital objects
that are already published on institutional web sites that could be
uploaded to a repository. This recopilation is a tedious task and
is usually performed manually. In this works we propose a system
architecture for collecting text documents in Spanish or English
to assist the manager of institutional repositories in the
recopilation task of TLO's within a restricted website. Thus,
plausible documents to be uploaded to a repository can be
detected. Also, its metadata such as title, category, author,
language, keywords and relevant contact data, are automatically
extracted in order to ask the author for the publication of his/her
document. A prototype of this system was developed and a case
study at Universidad Nacional de Rosario (UNR) is analyzed.

Full Documentation
--
Under the /Doc folder a paper about this work (english version) and the complete spanish version can be found.

Usage
--
The system can be configured from the Parameters.xml file. 
Inside the *<SEEDS>* tags under the *<crawlerParameters>*, 
URL's to crawl are expected (common separated values). 
This ones are crawled looking for PDF files, which are downloaded and processed 
to extract its metadata with differents tools. The web site structure is persisted 
in a graph database using Neo4j.

The correct use of *<postCrawlGraphTraversal>* and *<postCrawlLabeling>* values are explained in the Doc.

There are three local configurations which are mandatory to execute the program:

- Neo4j Home Installation Directory must be included under *<parsCit_Home>* tag. More info at http://www.neo4j.org/
- ParsCit Home Installation Directory must be included under *<parsCit_Home>* tag. More info at http://aye.comp.nus.edu.sg/parsCit/
- Valid Alchemy API activation key must be included under *<alchemyApiKey>* tag. A Quote limited key can be requested at http://www.alchemyapi.com/
