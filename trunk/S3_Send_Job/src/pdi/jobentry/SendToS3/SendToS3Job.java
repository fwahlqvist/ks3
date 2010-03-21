/*
 * Created on 18-03-2010
 * Plugin code : Vincent Teyssier
 * Plugin : Send files to Amazon S3
 */
package pdi.jobentry.SendToS3;

import java.io.File;
import java.io.FileNotFoundException;

import org.jets3t.service.Constants;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.EmailAddressGrantee;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.multithread.DownloadPackage;
import org.jets3t.service.multithread.S3ServiceSimpleMulti;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSDevPayCredentials;
import org.jets3t.service.utils.ServiceUtils;


import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.logging.LogWriter;


public class SendToS3Job {

  private String _AKey;
  private String _PKey;
  private String _S3Buck;
  private String _FileNm;

  public SendToS3Job(String AKey, String PKey, String S3Buck, String FileNm) {
    _AKey= AKey;
    _PKey = PKey;
    _S3Buck = S3Buck;
    _FileNm = FileNm;
  }
  
  //public long process()
  public void process() throws Exception {

	  LogWriter log = LogWriter.getInstance();
	  log.logDetailed(toString(), "Sending file to S3 Job    ");
	  log.logDetailed(toString(), "_____________________________________");
	  log.logDetailed(toString(), "Access Key\t : " + _AKey);
	  log.logDetailed(toString(), "Private Key\t : " + "Keep it secret !");
	  log.logDetailed(toString(), "End  Bucket\t : " + _S3Buck);
	  log.logDetailed(toString(), "Filename\t : " + _FileNm);
	  log.logDetailed(toString(), "_____________________________________");
	  
	  SendToS3();
  }
  
  public void SendToS3() throws Exception {
	  
	LogWriter log = LogWriter.getInstance();
  	
	AWSCredentials awsCredentials = new AWSCredentials(_AKey,_PKey);
  	
	S3Service s3Service = new RestS3Service(awsCredentials);
      
  	S3Bucket[] myBuckets = s3Service.listAllBuckets();
  	if(myBuckets != null){ 
  		log.logDetailed(toString(),"Connected to S3 !");
  		log.logDetailed(toString(), "_____________________________________");
  	}
  	log.logDetailed(toString(),"==>You have " + myBuckets.length + " Buckets in your S3" );
  	log.logDetailed(toString(),"==>You will send the file [" + _FileNm + "] to the bucket [" + _S3Buck + "]" );
  	
  	String TargetBucket = _S3Buck;
      
    File fileData = new File(_FileNm);
  	S3Object fileObject = new S3Object(fileData);
  	log.logDetailed(toString(),"==>Hash value: " + fileObject.getMd5HashAsHex());
  	log.logDetailed(toString(),"==>S3Object before upload: " + fileObject);
  	log.logDetailed(toString(),"_____________________________________");

      // Upload the data objects.
    s3Service.putObject(TargetBucket, fileObject);
    log.logDetailed(toString(),"Sending file to S3 ...");
  	log.logDetailed(toString(),"_____________________________________");
  	log.logDetailed(toString(),"==>S3Object after upload: " + fileObject);
  	log.logDetailed(toString(),"                                      ");
      
	}
}
