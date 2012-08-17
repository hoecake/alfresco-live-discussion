function main()
{
        // Create XML object to pull values from
        // configuration file
        var conf = new XML(config.script);
          
        // Use the defaults from the XML configuration file
        title = conf.title[0].toString();

        // call the repository to get the latest document
        
        var json = remote.call("/cignex/addons/discussioninvite");
        if (json.status == 200)
        {
        	obj = eval("(" + json + ")");
        	if(obj.error != null){
        		model.error = obj.error; 
        	} else {
            	model.result = obj;
        	}
        }
        else
        {
            model.error = "Response Status: "+json.status;
        }
  
        model.title = title;
}

main();