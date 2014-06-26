/******************************************************************
 * File:        PatchAll.java
 * Created by:  Dave Reynolds
 * Created on:  25 Jun 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.registry.utility;

import org.apache.jena.riot.RDFDataMgr;

import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.Closure;

/**
 * Utility to patch a set of entities in a registry from
 * a single source file containing multiple (managed) entities.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Update {
    Updater updater;
    
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: Update repository userid key sourcefile");
            System.exit(1);
        }
        
        String registry = NameUtils.ensureLastSlash( args[0] );
        String userid = args[1];
        String key = args[2];
        String source = args[3];
        
        Updater updater = new Updater(registry); 
        
        updater.login(userid, key);
        
        Model model = RDFDataMgr.loadModel(source);
        for (ResIterator ri = model.listSubjects(); ri.hasNext(); ) {
            Resource root = ri.next();
            if (root.isURIResource()) {
                Model entityModel = Closure.closure(root, false);
                updater.update( root.inModel(entityModel) );
            }
        }

        updater.logout();
    }

}
