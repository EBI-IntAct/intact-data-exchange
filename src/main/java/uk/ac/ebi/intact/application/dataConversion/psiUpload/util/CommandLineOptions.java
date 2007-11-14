/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 *//**
  * That class .
  *
  * @author Samuel Kerrien (skerrien@ebi.ac.uk)
  * @version $Id$
  */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.util;

public class CommandLineOptions {

    private String defaultInteractorTaxid = null;
    private String defaultInteractionType = null;
    private boolean reuseProtein = false;
    private boolean debugEnabled = false;
    private boolean guiEnabled = false;


    private static CommandLineOptions ourInstance = new CommandLineOptions();

    public static CommandLineOptions getInstance() {
        return ourInstance;
    }

    private CommandLineOptions() {
    }


    ///////////////////////////
    // Getters and Setters

    public boolean hasDefaultInteractorTaxid() {
        return defaultInteractorTaxid != null;
    }

    public String getDefaultInteractorTaxid() {
        return defaultInteractorTaxid;
    }

    public void setDefaultInteractorTaxid( final String s ) {
        defaultInteractorTaxid = s;
    }


    public boolean hasDefaultInteractionType() {
        return defaultInteractionType != null;
    }

    public String getDefaultInteractionType() {
        return defaultInteractionType;
    }

    public void setDefaultInteractionType( String s ) {
        defaultInteractionType = s;
    }


    public void setReuseExistingProtein( final boolean flag ) {
        reuseProtein = flag;
    }

    public boolean reuseProtein() {
        return reuseProtein;
    }


    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled( final boolean debugEnabled ) {
        this.debugEnabled = debugEnabled;
    }

    public void setGuiEnabled( boolean guiEnabled ) {
        this.guiEnabled = guiEnabled;
    }

    public boolean isGuiEnabled() {
        return guiEnabled;
    }
}


