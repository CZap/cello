package org.cellocad.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.tools.ant.DirectoryScanner;
import org.cellocad.MIT.dnacompiler.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by Bryan Der on 7/3/15.
 */



@RestController
public class FileController extends BaseController {


    private final Logger logger = LoggerFactory.getLogger(FileController.class);


    @RequestMapping(value="/results",method= RequestMethod.GET)
    public @ResponseBody
    String[] getDirectoryList(
            @RequestHeader("Authorization") String basic
    ) throws IOException {

        if(!auth.login(basic)) {
            throw new CelloUnauthorizedException("invalid username/password");
        }
        String username = auth.getUsername(basic);
        
        String directory = _resultPath + "/" + username;

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(directory);
        scanner.scan();

        String[] directory_names = scanner.getIncludedDirectories();
        return directory_names;
    }


    @RequestMapping(value="/results/{jobid}",method= RequestMethod.GET)
    public @ResponseBody
    String[] getResultFiles(
            @RequestHeader("Authorization") String basic,
            @PathVariable("jobid") String jobid,
            @RequestParam Map<String, String> params
    ) throws IOException {

        if(!auth.login(basic)) {
            throw new CelloUnauthorizedException("invalid username/password");
        }
        String username = auth.getUsername(basic);

        String keyword = "";
        String extension = "";

        if(params.containsKey("keyword")) {
            keyword = params.get("keyword");
        }
        if(params.containsKey("extension")) {
            extension = params.get("extension");
        }

        String filePath = _resultPath + "/" + username + "/" + jobid;

        File f = new File(filePath);
        if (f.exists()) {

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setIncludes(new String[]{"*" + keyword + "*" + extension});
            scanner.setBasedir(filePath);
            scanner.setCaseSensitive(false);
            scanner.scan();
            String[] files = scanner.getIncludedFiles();
            return files;
        } else {
            return null;
        }
    }

    @RequestMapping(value="/results/{jobid}",method= RequestMethod.DELETE)
    public @ResponseBody
    String deleteDirectory(
            @RequestHeader("Authorization") String basic,
            @PathVariable("jobid") String jobid
    ) throws IOException {

        if(!auth.login(basic)) {
            throw new CelloUnauthorizedException("invalid username/password");
        }
        String username = auth.getUsername(basic);

        String directory = _resultPath + "/" + username + "/" + jobid;

        //trying to do this safely, in other words, not deleting subdirectories or recursive delete
        Util.deleteFilesInDirectory(new File(directory));
        Util.deleteEmptyDirectory(new File(directory));

        return "deleted " + directory;
    }



    @RequestMapping(value="/results/{jobid}/{filename:.+}", method = RequestMethod.GET)
    public @ResponseBody
    String getResultFile(
            @RequestHeader("Authorization") String basic,
            @PathVariable("jobid") String jobid,
            @PathVariable("filename") String filename
    ) {

        if(!auth.login(basic)) {
            throw new CelloUnauthorizedException("invalid username/password");
        }
        String username = auth.getUsername(basic);

        if(filename.endsWith(".png")) {
            String filePath = _resultPath + "/" + username + "/" + jobid + "/" + filename;
            try {
                //Convert binary image file to byte array to base64 encoded string
                FileInputStream mFileInputStream = new FileInputStream(filePath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = mFileInputStream.read(b)) != -1) {
                    bos.write(b, 0, bytesRead);
                }
                byte[] ba = bos.toByteArray();
                String imgstr = Base64.encodeBase64String(ba);
                mFileInputStream.close();
                bos.close();
                return imgstr;
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        else {

            String filePath = _resultPath + "/" + username + "/" + jobid + "/" + filename;
            String fileContents = "";
            try {
                fileContents = readFile(filePath, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return fileContents;
        }
    }
    private static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    @RequestMapping(value="/resultsroot",method= RequestMethod.GET)
    public @ResponseBody
    String getResultRootPath(
            @RequestHeader("Authorization") String basic
    ) throws IOException {

        if (!auth.login(basic)) {
            throw new CelloUnauthorizedException("invalid username/password");
        }

        return _resultPath;
    }



}
