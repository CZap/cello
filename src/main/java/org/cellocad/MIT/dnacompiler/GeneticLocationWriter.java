package org.cellocad.MIT.dnacompiler;


import org.cellocad.adaptors.ucfadaptor.UCFAdaptor;

import java.util.ArrayList;
import java.util.HashSet;

public class GeneticLocationWriter {


    public static void set_bp_range_for_parts(ArrayList<Part> parts, int start_bp, int end_bp) {
        for(Part p: parts) {
            p.set_start(start_bp);
            p.set_end(end_bp);
        }
    }

    public static void insertModulePartsIntoGeneticLocations(LogicCircuit lc, UCF ucf) {

        UCFAdaptor ucf_adaptor = new UCFAdaptor();

        if(!ucf_adaptor.getLocationName(ucf, "sensor").equals("")) {
            for (ArrayList<Part> sensor_module : lc.get_sensor_module_parts() )  {
                Integer start_bp = ucf_adaptor.getLocationStartBP(ucf, "sensor");
                Integer end_bp   = ucf_adaptor.getLocationStartBP(ucf, "sensor");
                GeneticLocationWriter.set_bp_range_for_parts(sensor_module, start_bp, end_bp);
            }
        }

        if(!ucf_adaptor.getLocationName(ucf, "circuit").equals("")) {
            for (ArrayList<Part> circuit_module : lc.get_circuit_module_parts()) {
                Integer start_bp = ucf_adaptor.getLocationStartBP(ucf, "circuit");
                Integer end_bp   = ucf_adaptor.getLocationStartBP(ucf, "circuit");
                GeneticLocationWriter.set_bp_range_for_parts(circuit_module, start_bp, end_bp);
            }
        }

        if(!ucf_adaptor.getLocationName(ucf, "output").equals("")) {
            for (ArrayList<Part> output_module : lc.get_output_module_parts()) {
                Integer start_bp = ucf_adaptor.getLocationStartBP(ucf, "output");
                Integer end_bp   = ucf_adaptor.getLocationStartBP(ucf, "output");
                GeneticLocationWriter.set_bp_range_for_parts(output_module, start_bp, end_bp);
            }
        }


        String sensor_location  = ucf_adaptor.getLocationName(ucf, "sensor");
        String circuit_location = ucf_adaptor.getLocationName(ucf, "circuit");
        String output_location  = ucf_adaptor.getLocationName(ucf, "output");

        if(!sensor_location.equals("")) {
            //if sensor module has its own unique location
            if (!sensor_location.equals(circuit_location) && !sensor_location.equals(output_location)) {
                writeModulesToLocation(
                        ucf,
                        "sensor",
                        lc.get_sensor_module_parts(),
                        lc.get_sensor_plasmid_parts() //the list of plasmids is generated by fn call
                );
            }
        }

        if(!circuit_location.equals("")) {
            //if circuit module has its own unique location
            if (!circuit_location.equals(sensor_location) && !circuit_location.equals(output_location)) {
                writeModulesToLocation(
                        ucf,
                        "circuit",
                        lc.get_circuit_module_parts(),
                        lc.get_circuit_plasmid_parts() //the list of plasmids is generated by fn call
                );
            }
        }

        if(!output_location.equals("")) {
            //if output module has its own unique location
            if (!output_location.equals(circuit_location) && !output_location.equals(sensor_location)) {
                writeModulesToLocation(
                        ucf,
                        "output",
                        lc.get_output_module_parts(),
                        lc.get_output_plasmid_parts() //the list of plasmids is generated by fn call
                );
            }
        }

        if(!sensor_location.equals("") && !sensor_location.equals("")) {
            //if sensor and circuit share a location
            if (sensor_location.equals(circuit_location) && !sensor_location.equals(output_location)) {
                writeModulesToLocation(
                        ucf,
                        "sensor",
                        lc.get_sensor_module_parts(),
                        "circuit",
                        lc.get_circuit_module_parts(),
                        lc.get_circuit_plasmid_parts() //the list of plasmids is generated by fn call
                );
            }
        }

        if(!sensor_location.equals("") && !output_location.equals("")) {
            //if sensor and output share a location
            if (sensor_location.equals(output_location) && !sensor_location.equals(circuit_location)) {
                writeModulesToLocation(
                        ucf,
                        "sensor",
                        lc.get_sensor_module_parts(),
                        "output",
                        lc.get_output_module_parts(),
                        lc.get_output_plasmid_parts() //the list of plasmids is generated by fn call
                );
            }
        }

        if(!circuit_location.equals("") && !output_location.equals("")) {
            //if circuit and output share a location
            if (circuit_location.equals(output_location) && !circuit_location.equals(sensor_location)) {
                writeModulesToLocation(
                        ucf,
                        "circuit",
                        lc.get_circuit_module_parts(),
                        "output",
                        lc.get_output_module_parts(),
                        lc.get_circuit_plasmid_parts() //the list of plasmids is generated by fn call
                );
            }
        }

        if(!sensor_location.equals("") && !circuit_location.equals("") && !output_location.equals("")) {
            //if sensor and circuit and output share a location


            if (circuit_location.equals(output_location) && circuit_location.equals(sensor_location)) {
                writeModulesToLocation(
                        ucf,
                        "sensor",
                        lc.get_sensor_module_parts(),
                        "circuit",
                        lc.get_circuit_module_parts(),
                        "output",
                        lc.get_output_module_parts(),
                        lc.get_circuit_plasmid_parts() //the list of plasmids is generated by fn call
                );
            }
        }

    }

    
    public static void writeModulesToLocation(
            UCF ucf,
            String module_name,
            ArrayList<ArrayList<Part>> module_parts,
            ArrayList<ArrayList<Part>> plasmid_list
    ) {


        UCFAdaptor ucf_adaptor = new UCFAdaptor();

        ArrayList<String> genbank_lines = ucf_adaptor.getLocationGenbankLines(ucf, module_name);
        Integer start_bp = ucf_adaptor.getLocationStartBP(ucf, module_name);
        Integer end_bp = ucf_adaptor.getLocationEndBP(ucf, module_name);

        String backbone_seq = Plasmid.extractNucleotideSequenceFromGenbankLines(genbank_lines);

        //Step 1. Make a sorted list of unique cut sites
        HashSet<Integer> cut_sites_map = new HashSet<Integer>();
        cut_sites_map.add(1);
        cut_sites_map.add(start_bp);
        cut_sites_map.add(end_bp);
        cut_sites_map.add(backbone_seq.length());
        ArrayList<Integer> cut_sites = new ArrayList<Integer>(cut_sites_map);
        java.util.Collections.sort(cut_sites);

        System.out.println(cut_sites.toString());

        //Step 2. Divide the backbone into segments based on the cuts.  Excise gaps when appropriate.
        ArrayList<Part> backbone_parts = new ArrayList<Part>();
        for(int i=0; i<cut_sites.size()-1; ++i) {
            if(cut_sites.get(i+1) > cut_sites.get(i)) {
                Part p = new Part("backbone", "backbone", backbone_seq.substring(cut_sites.get(i)-1, cut_sites.get(i+1)-1));
                p.set_start(cut_sites.get(i));
                p.set_end(cut_sites.get(i+1));

                //excision of gap sequence
                if(p.get_start() == start_bp && p.get_end() == end_bp) {
                    continue;
                }

                backbone_parts.add(p);
            }
        }

        for(ArrayList<Part> module: module_parts) {

            //Step 3. Assign the part start/end bp numbers according to the genetic location start/end from the UCF.
            //Note that all parts will share the same start/end.
            //Sorting parts based on start/end values will maintain the current order if start/end are identical.
            for (Part p : module) {
                p.set_start(start_bp);
                p.set_end(end_bp);
            }

            ArrayList<Part> plasmid = new ArrayList<Part>();
            plasmid.addAll(module);
            for(Part p: backbone_parts) {
                plasmid.add(new Part(p));
            }

            //Step 4. Sort parts based on start/end bp number, then renumber all parts in the plasmid.
            //Sorting MUST maintain the intended part order.
            Plasmid.sortPartsByStartBP(plasmid);

            Plasmid.renumberPlasmidBases(plasmid, 0);

            //Step 5. Append plasmid to list of plasmids.
            plasmid_list.add(plasmid);
        }
    }


    public static void writeModulesToLocation(
            UCF ucf,
            String module_name_1,
            ArrayList<ArrayList<Part>> module_parts_1,
            String module_name_2,
            ArrayList<ArrayList<Part>> module_parts_2,
            ArrayList<ArrayList<Part>> plasmid_list
    ) {

        UCFAdaptor ucf_adaptor = new UCFAdaptor();

        ArrayList<String> genbank_lines_1 = ucf_adaptor.getLocationGenbankLines(ucf, module_name_1);
        Integer start_bp_1 = ucf_adaptor.getLocationStartBP(ucf, module_name_1);
        Integer end_bp_1 = ucf_adaptor.getLocationEndBP(ucf, module_name_1);

        ArrayList<String> genbank_lines_2 = ucf_adaptor.getLocationGenbankLines(ucf, module_name_2);
        Integer start_bp_2 = ucf_adaptor.getLocationStartBP(ucf, module_name_2);
        Integer end_bp_2 = ucf_adaptor.getLocationEndBP(ucf, module_name_2);

        String backbone_seq = Plasmid.extractNucleotideSequenceFromGenbankLines(genbank_lines_1);

        //Step 1. Make a sorted list of unique cut sites
        HashSet<Integer> cut_sites_map = new HashSet<Integer>();
        cut_sites_map.add(1);
        cut_sites_map.add(start_bp_1);
        cut_sites_map.add(end_bp_1);
        cut_sites_map.add(start_bp_2);
        cut_sites_map.add(end_bp_2);
        cut_sites_map.add(backbone_seq.length());
        ArrayList<Integer> cut_sites = new ArrayList<Integer>(cut_sites_map);
        java.util.Collections.sort(cut_sites);

        System.out.println(cut_sites.toString());

        //Step 2. Divide the backbone into segments based on the cuts.  Excise gaps when appropriate.
        ArrayList<Part> backbone_parts = new ArrayList<Part>();
        for(int i=0; i<cut_sites.size()-1; ++i) {
            if(cut_sites.get(i+1) > cut_sites.get(i)) {
                Part p = new Part("backbone", "backbone", backbone_seq.substring(cut_sites.get(i)-1, cut_sites.get(i+1)-1));
                p.set_start(cut_sites.get(i));
                p.set_end(cut_sites.get(i+1));

                //excision of gap sequence
                if(p.get_start() == start_bp_1 && p.get_end() == end_bp_1) {
                    continue;
                }
                if(p.get_start() == start_bp_2 && p.get_end() == end_bp_2) {
                    continue;
                }

                backbone_parts.add(p);
            }
        }

        for(ArrayList<Part> module1: module_parts_1) {

            for(ArrayList<Part> module2: module_parts_2) { //nested loop will enumerate all combinations of module variants

                //Step 3. Assign the part start/end bp numbers according to the genetic location start/end from the UCF.
                //Note that all parts will share the same start/end.
                //Sorting parts based on start/end values will maintain the current order if start/end are identical.
                for (Part p : module1) {
                    p.set_start(start_bp_1);
                    p.set_end(end_bp_1);
                }
                for (Part p : module2) {
                    p.set_start(start_bp_2);
                    p.set_end(end_bp_2);
                }

                ArrayList<Part> plasmid = new ArrayList<Part>();
                plasmid.addAll(module1);
                plasmid.addAll(module2);
                for(Part p: backbone_parts) {
                    plasmid.add(new Part(p));
                }


                //Step 4. Sort parts based on start/end bp number, then renumber all parts in the plasmid.
                //Sorting MUST maintain the intended part order.
                Plasmid.sortPartsByStartBP(plasmid);

                Plasmid.renumberPlasmidBases(plasmid, 0);

                //Step 5. Append plasmid to list of plasmids.
                plasmid_list.add(plasmid);

            }
        }

    }


    public static void writeModulesToLocation(
            UCF ucf,
            String module_name_1,
            ArrayList<ArrayList<Part>> module_parts_1,
            String module_name_2,
            ArrayList<ArrayList<Part>> module_parts_2,
            String module_name_3,
            ArrayList<ArrayList<Part>> module_parts_3,
            ArrayList<ArrayList<Part>> plasmid_list
    ) {

        UCFAdaptor ucf_adaptor = new UCFAdaptor();

        ArrayList<String> genbank_lines_1 = ucf_adaptor.getLocationGenbankLines(ucf, module_name_1);
        Integer start_bp_1 = ucf_adaptor.getLocationStartBP(ucf, module_name_1);
        Integer end_bp_1 = ucf_adaptor.getLocationEndBP(ucf, module_name_1);

        ArrayList<String> genbank_lines_2 = ucf_adaptor.getLocationGenbankLines(ucf, module_name_2);
        Integer start_bp_2 = ucf_adaptor.getLocationStartBP(ucf, module_name_2);
        Integer end_bp_2 = ucf_adaptor.getLocationEndBP(ucf, module_name_2);

        ArrayList<String> genbank_lines_3 = ucf_adaptor.getLocationGenbankLines(ucf, module_name_3);
        Integer start_bp_3 = ucf_adaptor.getLocationStartBP(ucf, module_name_3);
        Integer end_bp_3 = ucf_adaptor.getLocationEndBP(ucf, module_name_3);
        
        String backbone_seq = Plasmid.extractNucleotideSequenceFromGenbankLines(genbank_lines_1);

        //Step 1. Make a sorted list of unique cut sites
        HashSet<Integer> cut_sites_map = new HashSet<Integer>();
        cut_sites_map.add(1);
        cut_sites_map.add(start_bp_1);
        cut_sites_map.add(end_bp_1);
        cut_sites_map.add(start_bp_2);
        cut_sites_map.add(end_bp_2);
        cut_sites_map.add(start_bp_3);
        cut_sites_map.add(end_bp_3);
        cut_sites_map.add(backbone_seq.length());
        ArrayList<Integer> cut_sites = new ArrayList<Integer>(cut_sites_map);
        java.util.Collections.sort(cut_sites);

        System.out.println(cut_sites.toString());

        //Step 2. Divide the backbone into segments based on the cuts.  Excise gaps when appropriate.
        ArrayList<Part> backbone_parts = new ArrayList<Part>();
        for(int i=0; i<cut_sites.size()-1; ++i) {
            if(cut_sites.get(i+1) > cut_sites.get(i)) {
                Part p = new Part("backbone", "backbone", backbone_seq.substring(cut_sites.get(i)-1, cut_sites.get(i+1)-1));
                p.set_start(cut_sites.get(i));
                p.set_end(cut_sites.get(i+1));

                //excision of gap sequence
                if(p.get_start() == start_bp_1 && p.get_end() == end_bp_1) {
                    continue;
                }
                if(p.get_start() == start_bp_2 && p.get_end() == end_bp_2) {
                    continue;
                }
                if(p.get_start() == start_bp_3 && p.get_end() == end_bp_3) {
                    continue;
                }

                backbone_parts.add(p);
            }
        }

        int counter = 0;

        for(ArrayList<Part> module1: module_parts_1) {

            for(ArrayList<Part> module2: module_parts_2) {

                for(ArrayList<Part> module3: module_parts_3) { //nested loop will enumerate all combinations of module variants

                    //Step 3. Assign the part start/end bp numbers according to the genetic location start/end from the UCF.
                    //Note that all parts will share the same start/end.
                    //Sorting parts based on start/end values will maintain the current order if start/end are identical.
                    for (Part p : module1) {
                        p.set_start(start_bp_1);
                        p.set_end(end_bp_1);
                    }
                    for (Part p : module2) {
                        p.set_start(start_bp_2);
                        p.set_end(end_bp_2);
                    }
                    for (Part p : module3) {
                        p.set_start(start_bp_3);
                        p.set_end(end_bp_3);
                    }
                    ArrayList<Part> plasmid = new ArrayList<Part>();
                    plasmid.addAll(module1);
                    plasmid.addAll(module2);
                    plasmid.addAll(module3);
                    for(Part p: backbone_parts) {
                        plasmid.add(new Part(p));
                    }

                    /*counter++;
                    for(Part p: plasmid) {
                        System.out.println("counter " + counter + " " + p.get_name() + " " + p.get_start() + " " + p.get_end());
                    }*/

                    //Step 4. Sort parts based on start/end bp number, then renumber all parts in the plasmid.
                    //Sorting MUST maintain the intended part order.
                    Plasmid.sortPartsByStartBP(plasmid);

                    Plasmid.renumberPlasmidBases(plasmid, 0);



                    //Step 5. Append plasmid to list of plasmids.
                    plasmid_list.add(plasmid);
                }

            }
        }


    }


}
