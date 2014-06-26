/******************************************************************
 * File:        Delete.java
 * Created by:  Dave Reynolds
 * Created on:  26 Jun 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.registry.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.epimorphics.util.NameUtils;

/**
 * Utility to delete a set of entries from registry.
 * Pass it a file with a list of URIs one per line.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Delete {
    Updater updater;
    
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: Delete repository userid key deletesFile");
            System.exit(1);
        }
        
        String registry = NameUtils.ensureLastSlash( args[0] );
        String userid = args[1];
        String key = args[2];
        String source = args[3];
        
        Updater updater = new Updater(registry); 
        
        updater.login(userid, key);
        
        try {
            BufferedReader in = new BufferedReader( new FileReader(source) );
            
            String line = null;
            
            while ( (line = in.readLine()) != null ) {
                line = line.trim();
                if (line.isEmpty()) continue;
                updater.delete( line );
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Failed to read: " + source);
        }

        updater.logout();
    }

}
