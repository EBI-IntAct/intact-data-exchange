/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.gui;

import javax.swing.*;
import java.awt.*;

/**
 * That class allows us to display the progress of .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Monitor {

    private JProgressBar progressBar;
    private String name;
    private JFrame frame;
    private JLabel totalLabel;
    private JLabel currentLabel;
    private JLabel statusLabel;
    private int max;
    private int current = 0;

    public Monitor( final int max, final String name ) {

        if ( max <= 0 ) {
            throw new IllegalArgumentException( "The count has to be greater than 0, you gave" + max + "." );
        }

        this.name = name;
        this.max = max;

        init();
    }


    private void init() {
        frame = new JFrame( name );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        Container cp = frame.getContentPane();
        cp.setBackground( Color.white );
        cp.setLayout( new BoxLayout( cp, BoxLayout.Y_AXIS ) );

        progressBar = new JProgressBar( JProgressBar.HORIZONTAL, 0, max - 1 );
        progressBar.setStringPainted( true );
        progressBar.setBackground( Color.white );
        progressBar.setForeground( new Color( 255, 217, 119 ) ); // light orange
        progressBar.setStringPainted( true );

        totalLabel = new JLabel( "Total: " + max );
        totalLabel.setForeground( Color.orange.darker() );

        currentLabel = new JLabel( "Current:" + current );
        currentLabel.setForeground( Color.orange );

        statusLabel = new JLabel();
        statusLabel.setForeground( Color.gray.brighter() );

        cp.add( progressBar );
        cp.add( totalLabel );
        cp.add( currentLabel );
        cp.add( statusLabel );
        frame.pack();  // shrink-wrap alternative

        Dimension d = frame.getSize();
        frame.setSize( (int) ( d.getWidth() * 2 ),
                       (int) ( d.getHeight() * 1.2 ) );
    }

    public void show() {
        frame.setVisible( true );
    }

    public void hide() {
        frame.setVisible( false );
    }

    private String getWindowsTitle() {
        return progressBar.getString() + " - " + name;
    }

    public void updateProteinProcessedCound( final int newCount ) {

        if ( progressBar == null ) {
            throw new NullPointerException( "The progress bar hasn't been created." );
        }

        // update current element
        current = newCount;
        currentLabel.setText( "Current: " + current );
        currentLabel.update( frame.getGraphics() );

        // update the progress bar
        progressBar.setValue( current );
        frame.setTitle( getWindowsTitle() );
    }

    public void updateProteinProcessedCound( final int newCount, final String status ) {

        // update status
        setStatus( status );

        // update current element and progress bar
        updateProteinProcessedCound( newCount );
    }

    public void setStatus( final String status ) {

        // update status
        statusLabel.setText( status );
        statusLabel.update( frame.getGraphics() );
        frame.setTitle( getWindowsTitle() );
    }


    /**
     * D E M O
     */
    public void testIt( Monitor monitor ) {
        for ( int i = 0; i <= max; i++ ) {
            try {
                Thread.sleep( 3 );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
            monitor.updateProteinProcessedCound( i, "processing: " + i );
        }
        monitor.setStatus( "Test finished." );
    }

    public static void main( String[] args ) {

        Monitor monitor = new Monitor( 3000, "Protein update" );
        monitor.show();
        monitor.testIt( monitor );
    }
}
