/******************************************************************
 * File:        PatchAll.java
 * Created by:  Dave Reynolds
 * Created on:  25 Jun 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.registry.utility;

import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.MediaType;

import org.apache.jena.riot.RDFDataMgr;

import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.Closure;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;

/**
 * Utility to patch a set of entities in a registry from
 * a single source file containing multiple (managed) entities.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class PatchAll {
    protected static final String MEDIA_TYPE = "text/turtle";
    
    ApacheHttpClient client;
    String registry;
    String webapp;
    
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: PatchAll repository sourcefile userid key");
            System.exit(1);
        }
        
        String registry = NameUtils.ensureLastSlash( args[0] );
        String source = args[1];
        String userid = args[2];
        String key = args[3];
        
        PatchAll patcher = new PatchAll(registry);
        patcher.login(userid, key);
        patcher.patch(source);
        patcher.logout();
    }
    
    public PatchAll(String registry) {
        this.registry = registry;
        
        webapp = registry.replaceFirst("http://.*?/(.*/)", "$1");
    }
    
    public void patch(String source) {

        Model model = RDFDataMgr.loadModel(source);
        
        for (ResIterator ri = model.listSubjects(); ri.hasNext(); ) {
            Resource root = ri.next();
            if (root.isURIResource()) {
                Model entityModel = Closure.closure(root, false);
                String target = registry + root.getURI().replaceFirst("http://.*?/(.*)", "$1").replaceFirst(webapp, "");
                patch(target, entityModel);
            }
        }
    }
    
    protected void patch(String target, Model entityModel) {
        System.out.println("Updating " + target);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        entityModel.write(out, "Turtle");
        
        WebResource r = client.resource(target);
        ClientResponse response = r.type(MEDIA_TYPE).put(ClientResponse.class, out.toString());
        if (response.getStatus() >= 400) {
            System.out.println("Failed: " + response.getEntity(String.class)+ " (" + response.getStatus() + ")");
        }
    }
    
    public void login(String userid, String key) {
        System.out.println("Login " + registry + "system/security/apilogin");
        client = ApacheHttpClient.create();
        client.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);
        
        Form loginform = new Form();
        loginform.add("userid", userid);
        loginform.add("password", key);
        
        WebResource r = client.resource(registry + "system/security/apilogin");
        ClientResponse response = r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, loginform);
        if (response.getStatus() >= 400) {
            System.err.println("Login failed: " + response.getEntity(String.class)+ " (" + response.getStatus() + ")");
            System.exit(1);
        }
    }
    
    public void logout() {
        System.out.println("Logout");
    }
}
