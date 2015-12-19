package org.cellocad.adaptors.ucfwriters.ucf_writers_LacI_TetR;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cellocad.MIT.dnacompiler.Util;

import java.util.ArrayList;
import java.util.Map;


public class ConstraintFileWriter {

    public static void main(String args[]) {

    	
    	/*
    	 * A UCF is an array of json objects.  A UCF is created by appending the json objects of
    	 * each collection to the UCF json array.  Some collections just have a single object, 
    	 * such as 'header', but other collections have a list of objects, such as 'gates'.
    	 */
    	
        ArrayList<Map> all_json_objects = new ArrayList<>();
        all_json_objects.addAll(new collection_writer_header().getObjects());
        all_json_objects.addAll(new collection_writer_measurement_std().getObjects());
        all_json_objects.addAll(new collection_writer_logic_constraints().getObjects());
        all_json_objects.addAll(new collection_writer_gates().getObjects());
        all_json_objects.addAll(new collection_writer_response_functions().getObjects());
        all_json_objects.addAll(new collection_writer_gate_parts().getObjects());
        all_json_objects.addAll(new collection_writer_parts().getObjects());
        all_json_objects.addAll(new collection_writer_eugene_rules().getObjects());

        String rootPath = collection_writer.getRootPath();
        prettyPrintJSONArray(all_json_objects, rootPath + "/resources/UCF/TetRLacI.UCF.json", false);
    }


    public static void prettyPrintJSONArray(ArrayList<Map> objects, String filepath, boolean append) {

        String json_array = "";

        for(int i=0; i<objects.size(); ++i) {

            Map obj = objects.get(i);

            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            String json = gson.toJson(obj);

            json_array += json;


            if(i < objects.size() - 1) {
                json_array += ",\n";
            }


        }

        Util.fileWriter(filepath, "[\n" + json_array + "\n]\n", append);
    }


}