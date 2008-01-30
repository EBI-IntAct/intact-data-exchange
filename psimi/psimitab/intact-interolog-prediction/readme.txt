
###############################
                                  
  INTEROPORC PREDICTION MODULE 
                                  
###############################

http://biodev.extra.cea.fr/interoporc/

Magali Michaut
magali.michaut@cea.fr
magali.michaut.2005@ingenieurs-supelec.org
mmichaut@ebi.ac.uk
--
07/06/20 created
07/11/26 added a simple run for one species predictions
07/11/30 added a result file with all source interactions used in the process
08/01/25 structured this file (content) and added the new project page
--

You can find information on EBI website: http://www.ebi.ac.uk/~mmichaut/
and a document on this prediction programme: http://www.ebi.ac.uk/~mmichaut/documents/bio.pdf
but it may not be updated... ;-)


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

You can either use the module online (see I) or use the independant JAR file available on the project page (http://biodev.extra.cea.fr/interoporc/) (see II) or import the latest jar library and include it into your code to use more options (see III).

>>> I) Online
Go http://biodev.extra.cea.fr/interoporc/ and run analysis for the NCBI taxid you are interested in.
See all species of Integr8 on http://www.ebi.ac.uk/integr8/OrganismSearch.do?action=setOrganismSearchType&searchType=2&pageContext=207

>>> II) With an independant JAR file (including all dependancies)
This JAR file is downloadable from the project page http://biodev.extra.cea.fr/interoporc/data/interopor.tar.gz
Here is described a simple way to use this program to predict interactions for one species.

If you have a jar with all dependencies --> interologPrediction.jar:
1) create a directory for the predictions --> DIR
2) put the jar in it
3) put a mitab file in it with all interactions you want to use as source interactions from other species --> sourceInteractions.mitab (if you're asking what the mitab format could be, see the FAQ at the end)
4) download the orthologous clusters from ftp://ftp.ebi.ac.uk/pub/databases/integr8/porc/proc_gene.dat and put it in the directory --> porc_gene.dat
5) choose the NCBI taxid of the species you are interested in (for example Synecocystis is 1148, yeast is 4932, E. coli is 562 ... see all species of Integr8 on http://www.ebi.ac.uk/integr8/OrganismSearch.do?action=setOrganismSearchType&searchType=2&pageContext=207)
6) OPTION: you can put a log4j-property-file in the dir (you can copy-paste the example given below and put it in interologPrediction.log4j.properties file in the directory) --> interologPrediction.log4j.properties

Then, execute this command in the directory DIR with your taxid (instead of 1148):

java -ms500m -mx1000m -cp interologPrediction.jar uk.ac.ebi.intact.interolog.prediction.RunForOneSpecies . sourceInteractions.mitab porc_gene.dat 1148 interologPrediction.log4j.properties


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

Be aware that this program needs some space. I am used to running it with extended arguments to the VM (-ms500m -mx900m). On the other hand, it does not take too much time.
Running it on the global mitab file (merge of all Intact, MINT and DIP) and predicting interactions for all species present in it will take less than 5 minutes.



2) WHAT ARE THE RESULT FILES?
=============================
1. InteroPorc.predictedInteractions.mitab
Predicted interactions are described in InteroPorc.predictedInteractions.mitab in mitab format.

2. KnownInteractions.mitab
All interactions from the species you are interested in are in KnownInteractions.mitab in mitab format.

3. srcInteractionsUsed.txt
All source interactions used during the process are described in the srcInteractionsUsed.txt file.

4. interologPrediction.log
Comments are written in the interologPrediction.log file during the process if you have configured the log4j property file.

5. downCast.history.txt
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

log4j.rootCategory=DEBUG, R, A

# package/class specific config
#log4j.category.edu.ucla.mbi.imex.imexcentral=WARN

# ***** A is set to be a ConsoleAppender.
log4j.appender.A=org.apache.log4j.ConsoleAppender
# ***** A uses PatternLayout.
log4j.appender.A.layout=org.apache.log4j.PatternLayout
log4j.appender.A.layout.ConversionPattern=%d [%t] %-5p (%C{1},%L) - %m%n
log4j.appender.A.Threshold=WARN

# ***** R file appender
log4j.appender.R=org.apache.log4j.RollingFileAppender
log4j.appender.R.File=interologPrediction.log
log4j.appender.R.Threshold=INFO

log4j.appender.R.MaxFileSize=100KB
# Keep one backup file
log4j.appender.R.MaxBackupIndex=1

log4j.appender.R.layout=org.apache.log4j.PatternLayout
log4j.appender.R.layout.ConversionPattern=%d %-5p (%C{1},%L) - %m%n
#log4j.appender.R.layout.ConversionPattern=%p %t %c - %m%n



4) FAQ
========

* What is the mitab format?
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
