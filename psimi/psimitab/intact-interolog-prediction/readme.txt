
###############################
                                  
  INTEROPORC PREDICTION MODULE 
                                  
###############################

http://biodev.extra.cea.fr/interoporc/

Magali Michaut
michaut.bioinfo@gmail.com
magali.michaut.2005@ingenieurs-supelec.org
mmichaut@ebi.ac.uk
--
07/06/20 created
07/11/26 added a simple run for one species predictions
07/11/30 added a result file with all source interactions used in the process
08/01/25 structured this file (content) and added the new project page
08/03/27 added PSI25-XML result files info
08/04/04 added info on new options to run the tool
--

You can find information on EBI website: http://www.ebi.ac.uk/~mmichaut/
and a document on this prediction programme: http://www.ebi.ac.uk/~mmichaut/documents/bio.pdf
but it is not updated any longer... ;-)
otherwise try http://people.rez-gif.supelec.fr/mmichaut/


******************************
CONTENT
******************************
1. How to use the module?
2. What are the result files?
3. Log4j property file example
4. FAQ
5. License
******************************


1) HOW TO USE THE MODULE?
=========================

You can either use the module online (see I) 
or use the independant JAR file available on the project page (http://biodev.extra.cea.fr/interoporc/) (see II)
or import the latest jar library and include it into your code to use more options (see III).

>>> I) Online
Go to http://biodev.extra.cea.fr/interoporc/ and run analysis for the NCBI taxid you are interested in.
See all species of Integr8 on 
http://www.ebi.ac.uk/integr8/OrganismSearch.do?action=setOrganismSearchType&searchType=2&pageContext=207

>>> II) With an independant JAR file (including all dependancies)
This JAR file is downloadable from the project page http://biodev.extra.cea.fr/interoporc/data/interopor.tar.gz
Here is described a simple way to use this program to predict interactions for one species.

If you have a jar with all dependencies --> interologPrediction.jar:
1) create a directory for the predictions --> DIR
2) put the jar in it
3) put a MITAB25 file in it with all interactions you want to use as source interactions from other species --> sourceInteractions.mitab 
(if you're asking what the MITAB25 format could be, see the FAQ at the end)
4) download the orthologous clusters from ftp://ftp.ebi.ac.uk/pub/databases/integr8/porc/proc_gene.dat and put it in the directory --> porc_gene.dat
5) choose the NCBI taxid of the species you are interested in 
(for example Synechocystis is 1148, yeast is 4932, E. coli is 562 ... see all species of Integr8 on 
http://www.ebi.ac.uk/integr8/OrganismSearch.do?action=setOrganismSearchType&searchType=2&pageContext=207)
6) OPTION: you can put a log4j-property-file in the dir  --> interologPrediction.log4j.properties
(you can copy-paste the example given below and put it in interologPrediction.log4j.properties file in the directory)

Then, execute this command in the directory DIR with your taxid (instead of 1148):
Then you can use the tool with the main following options:
usage: Interoporc [OPTIONS]
Options:
 -o,--output-directory <file>   Directory where all files will be created
 -i,--mitab-file <file>         MITAB File (Release 2.5) with source
                                interactions
 -p,--porc-file <file>          PORC file with orthologous clusters
 -h,--help                      print this message
 -l <file>                      use given file for log
 -t,--taxid <int>               NCBI taxonomy identifier of the species
 
 Here are some examples:
	* To print options
java -cp interologPrediction.jar uk.ac.ebi.intact.interolog.prediction.RunForOneSpecies
java -cp interologPrediction.jar uk.ac.ebi.intact.interolog.prediction.RunForOneSpecies -h

	* To predict interactions for Synechocystis (taxid=1148)
java -ms500m -mx1000m -cp interologPrediction.jar uk.ac.ebi.intact.interolog.prediction.RunForOneSpecies -o . -i sourceInteractions.mitab -p porc_gene.dat -t 1148 -l interologPrediction.log4j.properties


>>> III) With the JAR available on EBI maven repos
http://www.ebi.ac.uk/~maven/m2repo/uk/ac/ebi/intact/dataexchange/psimi/intact-interolog-prediction/
http://www.ebi.ac.uk/~maven/m2repo_snapshots/uk/ac/ebi/intact/dataexchange/psimi/intact-interolog-prediction/2.0.0-SNAPSHOT/
You have to create an instance of InterologPrediction with the required working directory (where files will be created).
Then you can change some parameters if needed and finally just run it. An example is given below:

  InterologPrediction p = new InterologPrediction(workingDir);
  p.setClog(clogFile);
  p.setMitab(mitabFile);
  p.setPredictedinteractionsFileExtension(".mitab");
  p.setWriteDownCastHistory(true);
  
  p.setDownCastOnAllPresentSpecies(false);
  p.setClassicPorcFormat(false);
  p.setWriteDownCastHistory(true);
  p.setWriteSrcInteractions(true);
  ClogInteraction.setNB_LINES_MAX(100000);
  p.setWritePorcInteractions(false);
  p.setDownCastOnChildren(false);
  
  p.run();

Be aware that this program needs some space. I am used to running it with extended arguments to the VM (-ms500m -mx1000m).
On the other hand, it does not take too much time.
Running it on the global MITAB25 file (merge of all Intact, MINT and DIP) 
and predicting interactions for all species present in it will take less than 5 minutes.



2) WHAT ARE THE RESULT FILES?
=============================
1. InteroPorc.predictedInteractions.mitab / InteroPorc.predictedInteractions.xml
Predicted interactions are described in both PSIMI25-XML and MITAB25 formats
(PSI25-XML is obtained with option -x and if not too many interactions are predicted)

2. KnownInteractions.mitab / KnownInteractions.xml
Interactions extracted from the source interaction fils for the species you are interested in
are described in both PSIMI25-XML and MITAB25 formats.
(PSI25-XML is obtained with option -x and if not too many interactions are predicted)

3. AllInteractions.mitab / AllInteractions.xml
All interactions (known and predicted) are described in both PSIMI25-XML and MITAB25 formats.
(PSI25-XML is obtained with option -x and if not too many interactions are predicted)

4. srcInteractionsUsed.txt
All source interactions used during the process are described in the srcInteractionsUsed.txt file.

5. interologPrediction.log
Comments are written in the interologPrediction.log file during the process if you have configured the log4j property file.

6. downCast.history.txt
Some information about the constructed porc interactions are in the tabulated text file downCast.history.txt
porcA=id from the porc data
porcB=id from the porc data
prot1=number of proteins in cluster porcA
prot2=number of proteins in cluster porcB
sources=number of source interactions used to construct this porc interaction
inferences=number of interaction predicted thanks to this porc interaction

0. proteome_report.txt
The proteome_report.txt file is downloaded and used during the process. It is not a result file but rather an input file.
You can remove it or keep it in the directory so that it is not downloaded again next time.


If it is not clear, don't hesitate to contact me.
Have fun! :-)




3) LOG4J PROPERTY FILE EXAMPLE
===============================

log4j.rootCategory=INFO, R, A

# package/class specific config
log4j.category.psidev=ERROR

# ***** A is set to be a ConsoleAppender.
log4j.appender.A=org.apache.log4j.ConsoleAppender
log4j.appender.A.layout=org.apache.log4j.PatternLayout
log4j.appender.A.layout.ConversionPattern=%m%n
log4j.appender.A.Threshold=WARN

# ***** R file appender
log4j.appender.R=org.apache.log4j.RollingFileAppender
log4j.appender.R.File=interoporc.log
log4j.appender.R.MaxFileSize=1000KB
log4j.appender.R.MaxBackupIndex=0
log4j.appender.R.layout=org.apache.log4j.PatternLayout
log4j.appender.R.layout.ConversionPattern=%d - %m%n



4) FAQ
========

* What is the PSI25-XML format?
PSI25-XML is the standard molecular interaction data exchange format defined by the Proteomics Standards Initiative (PSI).
All information are on the PSI website: http://www.psidev.info/

* What is the MITAB25 format?
MITAB25 describes binary interactions, one pair of interactors per row. Columns are separated by tabulators.
Fore more information, see:
- a simple readme file ftp://ftp.ebi.ac.uk/pub/databases/intact/current/psimitab/README
- the Proteomics Standards Initiative (PSI) website http://www.psidev.info/


5) LICENSE
===========
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved.
  
 Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer. 

2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in
the documentation and/or other materials provided with the
distribution.
 
3. The name IntAct must not be used to endorse or promote products 
 derived from this software without prior written permission. For 
written permission, please contact intact-dev@ebi.ac.uk

4. Products derived from this software may not be called "IntAct"
nor may "IntAct" appear in their names without prior written
permission of the IntAct developers.

5. Redistributions of any form whatsoever must retain the following
acknowledgment:
 "This product includes software developed by IntAct 
 (http://www.ebi.ac.uk/intact)"
 
THIS SOFTWARE IS PROVIDED BY THE INTACT GROUP ``AS IS'' AND ANY
EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE INTACT GROUP OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
