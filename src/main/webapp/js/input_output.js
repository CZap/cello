//var vio = vio;


/**
 * fires on button click (verilog.html)
 */
function inputFormDialog() {

    //clear the previous data in case the user previously created a new input promoter
    $( '#new_input_form' ).each(function(){
        this.reset();
    });

    //open a dialog box that allows the user to enter promoter name, low REU, high REU, and DNA sequence.
    $("#new_input_div").dialog( 'open' );
}

/**
 * fires on button click (verilog.html)
 */
function outputFormDialog() {

    //clear the previous data in case the user previously created a new output gene
    $( '#new_output_form' ).each(function(){
        this.reset();
    });

    //open a dialog box that allows the user to enter the gene name, and DNA sequence.
    $("#new_output_div").dialog( 'open' );
}


/**
 * Called when new_input_form (dialog) is submitted (verilog.html)
 *
 * @returns {boolean}
 */
function addNewInput() {
    var prom_name = $('#add_input_name').val();
    var low_REU   = Number($('#add_input_lowREU').val());
    var high_REU  = Number($('#add_input_highREU').val());
    var sequence  = $('#add_input_sequence').val();

    //the input promoter specification will be written to a text file with 4 columns.
    //this text file will be read by Cello.
    //see the -input_promoters filepath option in Args.java.
    var input_data = prom_name + " " + low_REU + " " + high_REU + " " + sequence + "\n";

    //max and min can be specified in the html form, but this extra check is needed
    //to prevent the low REU from exceeding the high REU.
    if(low_REU >= high_REU) {
        var rhtml = "<div class='alert alert-danger'> low REU not less than high REU </div>";
        $('#dialog_pre').html(rhtml);
        $('#dialog').dialog({title: "Input promoters"});
        $('#dialog').dialog('open');
        return false;
    }

    //prevent overwriting.  user will have to delete the promoter before creating one with the same name.
    var x = document.getElementById("input_pulldown");
    for (var i = 1; i < x.length; i++) {
        if (x.options[i].text == prom_name) {
            var rhtml = "<div class='alert alert-danger'> promoter already defined </div>";
            $('#dialog_pre').html(rhtml);
            $('#dialog').dialog({title: "Input promoters"});
            $('#dialog').dialog('open');
            return false;
        }
    }


    //A file with a single line and 4 columns will be written to the user's directory.
    //The user's directory is checked for any files that begin with 'input_' in order to populate
    //the pulldown menu of input promoters.

    var filename = "input_" + prom_name.trim() + ".txt"; //naming convention
    $.ajax({
        url: "in_out/" + filename,
        type: "POST",
        headers: {
            "Authorization": "Basic " + btoa(sessionStorage.username + ":" + sessionStorage.password)
        },
        data: {
            filetext:  input_data
        },
        success: function (response) {
            var input_obj = {};
            input_obj.prom_name = prom_name;
            input_obj.lowreu  = low_REU;
            input_obj.highreu = high_REU;
            input_obj.dnaseq  = sequence;
            vio.inputs_list.push(input_obj); //defined in verilog.js
            addInputsToMenu(); //update the pulldown menu with the new entry
            $('#new_input_div').dialog('close');
        },
        error: function() {
        }
    });
}


/**
 * Called when new_output_form (dialog) is submitted (verilog.html)
 *
 * @returns {boolean}
 */
function addNewOutput() {

    var gene_name = $('#add_output_name').val();
    var sequence  = $('#add_output_sequence').val();

    var output_data = gene_name + " " + sequence + "\n";

    //prevent overwriting.  user will have to delete the gene before creating one with the same name.
    var x = document.getElementById("output_pulldown");
    for (var i = 1; i < x.length; i++) {
        if (x.options[i].text == gene_name) {
            var rhtml = "<div class='alert alert-danger'> output gene already defined </div>";
            $('#dialog_pre').html(rhtml);
            $('#dialog').dialog({title: "Output genes"});
            $('#dialog').dialog('open');
            return false;
        }
    }

    //A file with a single line and 2 columns will be written to the user's directory.
    //The user's directory is checked for any files that begin with 'output_' in order to populate
    //the pulldown menu of output genes.
    var filename = "output_" + gene_name.trim() + ".txt"; //naming convention
    $.ajax({
        url: "in_out/" + filename,
        type: "POST",
        headers: {
            "Authorization": "Basic " + btoa(sessionStorage.username + ":" + sessionStorage.password)
        },
        data: {
            filetext:  output_data
        },
        success: function (response) {
            var output_obj = {};
            output_obj.gene_name = $('#add_output_name').val();
            output_obj.dnaseq    = $('#add_output_sequence').val();
            vio.outputs_list.push(output_obj); //defined in verilog.js
            addOutputsToMenu(); //update the pulldown menu with the new entry
            $('#new_output_div').dialog('close');
        },
        error: function() {
        }
    });


}

/**
 * fires on button click (verilog.html)
 */
function deleteInputDialog() {
    $('#delete_input').dialog( 'open' );
}

/**
 * fires on button click (verilog.html)
 */
function deleteOutputDialog() {
    $('#delete_output').dialog( 'open' );
}


/**
 * Called when delete_input form (dialog) is submitted.
 * Deletes the input_xxx.txt file in the user's result directory.
 *
 * @returns {boolean}
 */
function deleteInputFile() { //called on button click 'delete'

    var filename = "input_" + $('#delete_input_name').val() + ".txt";
    $.ajax({
        url: "in_out/" + filename,
        type: "DELETE",
        headers: {
            "Authorization": "Basic " + btoa(sessionStorage.username + ":" + sessionStorage.password)
        },
        data: {
        },
        success: function (response) {

            getInputFiles();
            resetInputList();

            $('#delete_input').dialog('close');

            //clear form data
            $( '#delete_input_form' ).each(function(){
                this.reset();
            });

        },
        error: function() {
        }
    });
}

/**
 * Called when delete_output form (dialog) is submitted
 * Deletes the output_xxx.txt file in the user's result directory.
 *
 * @returns {boolean}
 */
function deleteOutputFile() { //called on button click 'delete'

    var filename = "output_" + $('#delete_output_name').val() + ".txt";
    $.ajax({
        url: "in_out/" + filename,
        type: "DELETE",
        headers: {
            "Authorization": "Basic " + btoa(sessionStorage.username + ":" + sessionStorage.password)
        },
        data: {
        },
        success: function (response) {

            getOutputFiles();
            resetOutputList();

            $('#delete_output').dialog('close');

            //clear form data
            $( '#delete_output_form' ).each(function(){
                this.reset();
            });


        },
        error: function(response) {
        }
    });
}



function saveInputs() {
    vio.inputs = [];
    var counter = 0;
    $('#inputs_table tr').each(function(){
        counter++;
        if(counter!=1) {
            var input_obj = {};
            input_obj.index     = $(this).find('td').eq(0).find('input').val();
            input_obj.prom_name = $(this).find('td').eq(1).find('input').val();
            input_obj.lowreu    = $(this).find('td').eq(2).find('input').val();
            input_obj.highreu   = $(this).find('td').eq(3).find('input').val();
            input_obj.dnaseq    = $(this).find('td').eq(4).find('input').val();
            vio.inputs.push(input_obj);
        }
    });
}

function saveOutputs() {
    vio.outputs = [];
    var counter = 0;
    $('#outputs_table tr').each(function(){
        counter++;
        if(counter!=1) {
            var output_obj = {};
            output_obj.index     = $(this).find('td').eq(0).find('input').val();
            output_obj.gene_name = $(this).find('td').eq(1).find('input').val();
            output_obj.dnaseq    = $(this).find('td').eq(2).find('input').val();
            vio.outputs.push(output_obj);
        }
    });
}

function loadInputs() {


    if (typeof vio.inputs === 'undefined' || vio.inputs.length == 0 ) {
        vio.inputs = [];
        return false;
    }

    for (var i = 0; i < vio.inputs.length; ++i) {
        var n = $('#inputs_table tr').length;
        addInput();
        var input_obj = vio.inputs[i];
        $('#input_index'   + n).val(n);
        $('#input_name'    + n).val(input_obj.prom_name);
        $('#input_lowreu'  + n).val(input_obj.lowreu);
        $('#input_highreu' + n).val(input_obj.highreu);
        $('#input_dnaseq'  + n).val(input_obj.dnaseq);
    }

    var x = document.getElementById("input_pulldown");
    for (var i = 1; i < x.length; i++) {
        for (var j = 0; j < vio.inputs.length; j++) {
            if (x.options[i].text == vio.inputs[j].prom_name) {
                x.remove(i);
            }
        }
    }
}

function loadOutputs() {

    if (typeof vio.outputs === 'undefined' || vio.outputs.length == 0 ) {
        vio.outputs = [];
        return false;
    }

    for (var i = 0; i < vio.outputs.length; ++i) {
        var n = $('#outputs_table tr').length;
        addOutput();
        var output_obj = vio.outputs[i];
        $('#output_index' + n).val(n);
        $('#output_name'  + n).val(output_obj.gene_name);
        $('#output_dnaseq'+ n).val(output_obj.dnaseq);
    }

    var x = document.getElementById("output_pulldown");
    for (var i = 1; i < x.length; i++) {
        for (var j = 0; j < vio.outputs.length; j++) {
            if (x.options[i].text == vio.outputs[j].gene_name) {
                x.remove(i);
            }
        }
    }
}


function addInputsToMenu() {

    var x = document.getElementById("input_pulldown");
    removeOptions(x);

    var custom = document.createElement("option");
    custom.text  = "choose";
    custom.value = "choose";
    x.add(custom);

    for(var i in vio.inputs_list) {
        var input_obj = vio.inputs_list[i];
        var name = input_obj.prom_name;
        var custom = document.createElement("option");
        custom.text = name;
        custom.value = name;
        x.add(custom);
    }
}

function addOutputsToMenu() {

    var x = document.getElementById("output_pulldown");
    removeOptions(x);

    var custom = document.createElement("option");
    custom.text  = "choose";
    custom.value = "choose";
    x.add(custom);

    for(var i in vio.outputs_list) {
        var output_obj = vio.outputs_list[i];
        var name = output_obj.gene_name;
        var custom = document.createElement("option");
        custom.text = name;
        custom.value = name;
        x.add(custom);
    }
}

function getInputFiles() {

    $.ajax({
        url: "in_out",
        type: "GET",
        headers: {
            "Authorization": "Basic " + btoa(sessionStorage.username + ":" + sessionStorage.password)
        },
        data: {
            keyword: "input_",
            extension: "txt"
        },
        success: function(response) {

            vio.inputs_list = [];
            var list_of_requests = [];

            var filenames = response;
            for(var i=0; i<filenames.length; ++i) {
                if(filenames[i]) { //false if empty string
                    var filename = filenames[i];
                    list_of_requests.push(
                        $.ajax({
                            url: "in_out/" + filename,
                            type: "GET",
                            headers: {
                                "Authorization": "Basic " + btoa(sessionStorage.username + ":" + sessionStorage.password)
                            },
                            data: {
                            },
                            success: function(response) {
                                var input_attr = response.split(" ");
                                var input_obj = {};
                                input_obj.prom_name    = String (input_attr[0]);
                                input_obj.lowreu  = Number (input_attr[1]);
                                input_obj.highreu = Number (input_attr[2]);
                                input_obj.dnaseq  = String (input_attr[3]).replace(/(\r\n|\n|\r)/gm,"");
                                vio.inputs_list.push(input_obj);
                            }
                        })
                    );
                }
            }

            $.when.apply(undefined, list_of_requests).then(function(){
                addInputsToMenu();
                loadInputs();
            });
        },
        error: function() {
        }
    });
}

function getOutputFiles() {

    $.ajax({
        url: "in_out",
        type: "GET",
        headers: {
            "Authorization": "Basic " + btoa(sessionStorage.username + ":" + sessionStorage.password)
        },
        data: {
            keyword: "output_",
            extension: "txt"
        },
        success: function(response) {

            vio.outputs_list = [];
            var list_of_requests = [];

            var filenames = response;
            for(var i=0; i<filenames.length; ++i) {
                if(filenames[i]) { //false if empty string
                    var filename = filenames[i];
                    list_of_requests.push(
                        $.ajax({
                            url: "in_out/" + filename,
                            type: "GET",
                            headers: {
                                "Authorization": "Basic " + btoa(sessionStorage.username + ":" + sessionStorage.password)
                            },
                            data: {
                            },
                            success: function(response) {
                                var output_attr = response.split(" ");
                                var output_obj = {};
                                output_obj.gene_name = String (output_attr[0]);
                                output_obj.dnaseq = String (output_attr[1]).replace(/(\r\n|\n|\r)/gm,"");
                                vio.outputs_list.push(output_obj);
                            }
                        })
                    );
                }
            }

            $.when.apply(undefined, list_of_requests).then(function(){
                addOutputsToMenu();
                loadOutputs();
            });
        },
        error: function() {
        }
    });
}


function setInputFromList() {

    var x = document.getElementById("input_pulldown");

    addInput();
    for(var i in vio.inputs_list) {
        var input_obj = vio.inputs_list[i];
        if(input_obj.prom_name == x.value) {
            vio.inputs.push(input_obj);
            $('#input_index'   + vio.inputs.length).val(vio.inputs.length);
            $('#input_name'    + vio.inputs.length).val(input_obj.prom_name);
            $('#input_lowreu'  + vio.inputs.length).val(input_obj.lowreu);
            $('#input_highreu' + vio.inputs.length).val(input_obj.highreu);
            $('#input_dnaseq'  + vio.inputs.length).val(input_obj.dnaseq);
            break;
        }
    }

    x.remove(x.selectedIndex);
    x.value = "choose";
    x.text  = "choose";
}

function setOutputFromList() {

    var x = document.getElementById("output_pulldown");

    addOutput();
    for(var i in vio.outputs_list) {
        var output_obj = vio.outputs_list[i];
        if(output_obj.gene_name == x.value) {
            vio.outputs.push(output_obj);
            $('#output_index'  + vio.outputs.length).val(vio.outputs.length);
            $('#output_name'   + vio.outputs.length).val(output_obj.gene_name);
            $('#output_dnaseq' + vio.outputs.length).val(output_obj.dnaseq);
            break;
        }
    }

    x.remove(x.selectedIndex);
    x.value = "choose";
    x.text  = "choose";
}


function removeOptions(selectbox) {
    for(var i=selectbox.options.length-1;i>=0;i--)
    {
        selectbox.remove(i);
    }
}

function deleterow(tableID) {
    var table = document.getElementById(tableID);
    table.deleteRow(table.rows.length - 1);
}

function resetInputList() {
    vio.inputs = [];
    var input_table = document.getElementById("inputs_table");
    while(input_table.rows.length > 1) {
        deleterow("inputs_table");
    }
    removeOptions(document.getElementById("input_pulldown"));
    addInputsToMenu();
}

function resetOutputList() {
    vio.outputs = [];
    var output_table = document.getElementById("outputs_table");
    while(output_table.rows.length > 1) {
        deleterow("outputs_table");
    }
    removeOptions(document.getElementById("output_pulldown"));
    addOutputsToMenu();
}


function addInput() {
    var n = $('#inputs_table tr').length;
    $('#inputs_table').append("<tr>" +
        "<td width='10%'><input id='input_index"  +n+"' type='text' value='"+n+"' readonly></td>" +
        "<td width='20%'><input id='input_name"   +n+"' type='text' required></td>" +
        "<td width='15%'><input id='input_lowreu" +n+"' min='0.001' max='100' type='number' step='any' required></td>" +
        "<td width='15%'><input id='input_highreu"+n+"' min='0.001' max='100' type='number' step='any' required></td>" +
        "<td width='40%'><input id='input_dnaseq" +n+"' type='text' required></td>"+
        "</tr>");
}

function addOutput() {
    var n = $('#outputs_table tr').length;
    $('#outputs_table').append("<tr>" +
        "<td width='10%'><input id='output_index" +n+"' type='text' value='"+n+"' readonly></td>" +
        "<td width='20%'><input id='output_name"  +n+"' type='text' required></td>" +
        "<td width='70%'><input id='output_dnaseq"+n+"' type='text' required></td>"+
        "</tr>");
}


//parse number of inputs and number of outputs from Verilog file
//need to make sure the user entered enough Input and Output gates in the I/O tab
function get_v_inputs() {
    var lines = vio.verilog.split(/\n/);

    for(var j=0; j<lines.length; ++j) {
        if(lines[j].indexOf("input") > -1) {
            var module_line = lines[j];
            var nowhitespace = module_line.replace(/\s/g, "");
            var input_substring = nowhitespace.split(/input/);
            var split_inputs = input_substring[1].split(/,|\)|;/);
            var inputs = [];
            for(var i=0; i<split_inputs.length; ++i) {
                if(split_inputs[i].length > 0) {
                    inputs.push(split_inputs[i]);
                }
            }
            return inputs.length;
        }
    }
}
function get_v_outputs() {
    var lines = vio.verilog.split(/\n/);
    for(var j=0; j<lines.length; ++j) {
        if(lines[j].indexOf("output") > -1) {
            var module_line = lines[j];
            var nowhitespace = module_line.replace(/\s/g, "");
            var output_substring = nowhitespace.split(/output|input/);
            var split_outputs = output_substring[1].split(/,|;/);
            var outputs = [];
            for (var i = 0; i < split_outputs.length; ++i) {
                if (split_outputs[i].length > 0) {
                    outputs.push(split_outputs[i]);
                }
            }
        }
    }
    return outputs.length;
}


/**
 * Returns a string for the 4-column table of input promoter data (name, lowREU, highREU, sequence)
 *
 * @returns {string}
 */
/*function getInputData() {
    var indata = "";
    $('#inputs_table tr').each(function(){
        $(this).find('td').each(function(){
            var data = $(this).find('input').val();
            if(data && !$(this).find('input').is('[readonly]')) {
                indata += data + " ";
            }

        });
        indata += "\n";
    })
    return indata;
}*/

/**
 * Returns a string for the 2-column table of output gene data (name, sequence)
 *
 * @returns {string}
 */
/*function getOutputData() {
    var outdata = "";
    $('#outputs_table tr').each(function(){
        $(this).find('td').each(function(){
            var data = $(this).find('input').val();
            if(data && !$(this).find('input').is('[readonly]')) {
                outdata += data + " ";
            }

        })
        outdata += "\n";
    });
    return outdata;
}*/

function makeInputTableText(inputs) {
    var table_text = "";
    for(var i=0; i<inputs.length; ++i) {
        var input = inputs[i];
        table_text += input.prom_name + " " + input.lowreu + " " + input.highreu + " " + input.dnaseq + "\n";
    }
    return table_text;
}


function makeOutputTableText(outputs) {
    var table_text = "";
    for(var i=0; i<outputs.length; ++i) {
        var output = outputs[i];
        table_text += output.gene_name + " " + output.dnaseq + "\n";
    }
    return table_text;
}