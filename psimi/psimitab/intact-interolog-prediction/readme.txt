
###############################
#                                                                                       #
# INTEROPORC PREDICTION PROGRAMME  #
#                                                                                       #
###############################

Magali Michaut
magali.michaut@cea.fr
mmichaut@ebi.ac.uk
--
created 07/06/20
last update 07/11/26
--

You can find information on EBI website: http://www.ebi.ac.uk/~mmichaut/
and a document on this prediction programme: http://www.ebi.ac.uk/~mmichaut/documents/bio.pdf
but it may not be updated... ;-)


HOW TO USE THE PROGRAMME?
=======================

Here is described a simple way to use this programme to predict interactions for one species. You can also import the jar lib and use more options.
http://www.ebi.ac.uk/~maven/m2repo_snapshots/uk/ac/ebi/intact/dataexchange/psimi/intact-interolog-prediction/2.0.0-SNAPSHOT/

If you have a jar with dependencies -->interologPrediction.jar:
1) create a directory for the predictions --> DIR
2) put the jar in it
3) put a mitab file in it with all interactions you want to use as source interactions from other species --> sourceInteractions.mitab
4) download the orthologous clusters from ftp://ftp.ebi.ac.uk/pub/databases/integr8/porc/proc_gene.dat and put it in the directory --> porc_gene.dat
5) choose the NCBI taxid of the species you are interested in (for example Synecocystis is 1148, yeast is 4932, E. coli is 562 ... see http://www.ebi.ac.uk/newt/display )
6) OPTION: you can put a log4j-property-file in the dir (you can copy-paste the example given below and put it in interologPrediction.log4j.properties file in the directory) --> interologPrediction.log4j.properties

Then, execute this command in the directory DIR with your taxid (instead of 1148):

java -ms500m -mx1000m -cp interologPrediction.jar uk.ac.ebi.intact.interolog.prediction.RunForOneSpecies . sourceInteractions.mitab porc_gene.dat 1148 interologPrediction.log4j.properties


RESULTS
======
1) predicted interactions are described in InteroPorc.predictedInteractions.mitab in mitab format
2) some information about the constructed porc interactions are in the tabulated text file downCast.history.txt
porcA=id from the porc data
porcB=id from the porc data
prot1=number of proteins in cluster porcA
prot2=number of proteins in cluster porcB
sources=number of source interactions used to construct this porc interaction
inferences=number of interaction predicted thanks to this porc interaction
3) comments are written in the interologPrediction.log file during the process if you have configured the log4j property file
4) the proteome_report.txt file is downloaded and used during the process. You can remove it or keep it in the directory so that it is not downloaded again next time.


If it is not clear, don't hesitate to contact me.
Have fun! :-)




LOG4J PROPERTY FILE EXAMPLE
=======================

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

log4j.appender.R.MaxFileSize=100KB
# Keep one backup file
log4j.appender.R.MaxBackupIndex=1

log4j.appender.R.layout=org.apache.log4j.PatternLayout
log4j.appender.R.layout.ConversionPattern=%d %-5p (%C{1},%L) - %m%n
#log4j.appender.R.layout.ConversionPattern=%p %t %c - %m%n


