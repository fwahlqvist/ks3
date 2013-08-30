/*******************************************************************************
 * * ** * This code belongs to the KETTLE project. ** * ** * Kettle, from
 * version 2.2 on, is released into the public domain ** * under the Lesser GNU
 * Public License (LGPL). ** * ** * For more details, please read the document
 * LICENSE.txt, included ** * in this project ** * ** * http://www.kettle.be ** *
 * info@kettle.be ** * **
 ******************************************************************************/

package pdi.jobentry.SendToS3;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

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


// Send files to Amazon S3
// Plugin Code : Vincent Teyssier


public class JobEntrySendToS3 extends JobEntryBase implements Cloneable, JobEntryInterface {
	
  private static final String ACCESSKEY = "accesskey";

  private static final String PRIVATEKEY = "privatekey";

  private static final String S3BUCKET = "s3bucket";
  
  private static final String FILENAMETOSEND = "filenametosend";

  private String AccessKey;
  
  private String PrivateKey;

  private String targetDirectory;

  private String S3Bucket;
  
  private String FilenameToSend;

  public final String getAccessKey() {
    return AccessKey;
  }

  public final void setAccessKey(String AccessKey) {
    this.AccessKey = AccessKey;
  }
  
  public final String getPrivateKey() {
	    return PrivateKey;
	  }

  public final void setPrivateKey(String PrivateKey) {
	    this.PrivateKey = PrivateKey;
	  } 

  public final String getS3Bucket() {
    return S3Bucket;
  }

  public final void setS3Bucket(String S3Bucket) {
    this.S3Bucket = S3Bucket;
  }

  public final String GetFilenameToSend(){
	  return FilenameToSend;
  }
  
  public final void SetFilenameToSend(String FilenameToSend){
	  this.FilenameToSend = FilenameToSend;
  }
    
  public JobEntrySendToS3(String n) {
    super(n, "");
    setID(-1L);
  }

  public JobEntrySendToS3() {
    this("");
  }

  public JobEntrySendToS3(JobEntryBase jeb) {
    super(jeb);
  }

  public Object clone() {
    JobEntrySendToS3 je = (JobEntrySendToS3) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append(super.getXML());

    retval.append("      " + XMLHandler.addTagValue(ACCESSKEY, AccessKey));
    retval.append("      " + XMLHandler.addTagValue(PRIVATEKEY, PrivateKey));
    retval.append("      " + XMLHandler.addTagValue(S3BUCKET, S3Bucket));
    retval.append("      " + XMLHandler.addTagValue(FILENAMETOSEND, FilenameToSend));

    return retval.toString();
  }
  
  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases, slaveServers);
      AccessKey = XMLHandler.getTagValue(entrynode, ACCESSKEY);
      PrivateKey = XMLHandler.getTagValue(entrynode, PRIVATEKEY);
      S3Bucket = XMLHandler.getTagValue(entrynode, S3BUCKET);
      FilenameToSend = XMLHandler.getTagValue(entrynode, FILENAMETOSEND);
    } catch (KettleXMLException xe) {
      throw new KettleXMLException("Unable to load file exists job entry from XML node",
          xe);
    }
  }
  
  @Override
  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
	  try {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      AccessKey = rep.getJobEntryAttributeString(id_jobentry, ACCESSKEY);
      PrivateKey = rep.getJobEntryAttributeString(id_jobentry, PRIVATEKEY);
      S3Bucket = rep.getJobEntryAttributeString(id_jobentry, S3BUCKET);
      FilenameToSend = rep.getJobEntryAttributeString(id_jobentry, FILENAMETOSEND);
    } catch (KettleException dbe) {
      throw new KettleException(
          "Unable to load job entry for type file exists from the repository for id_jobentry="
              + id_jobentry, dbe);
    }
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    try {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), ACCESSKEY, AccessKey);
      rep.saveJobEntryAttribute(id_job, getID(), PRIVATEKEY, PrivateKey);
      rep.saveJobEntryAttribute(id_job, getID(), S3BUCKET, S3Bucket);
      rep.saveJobEntryAttribute(id_job, getID(), FILENAMETOSEND, FilenameToSend);
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          "unable to save jobentry of type 'file exists' to the repository for id_job="
              + id_job, dbe);
    }
  }

  public Result execute(Result prev_result, int nr, Repository rep, Job parentJob) {
    LogWriter log = LogWriter.getInstance();

    Result result = new Result(nr);
    result.setResult(false);

    log.logDetailed(toString(), "Start of processing");

    // String substitution..
    String realS3Bucket = environmentSubstitute(S3Bucket);
    String realAccessKey = environmentSubstitute(AccessKey);
    String realPrivateKey = environmentSubstitute(PrivateKey);
    String realFilenameToSend = environmentSubstitute(FilenameToSend);
    SendToS3Job proc = new SendToS3Job(realAccessKey, realPrivateKey, realS3Bucket, realFilenameToSend);

    try {
      proc.process();	
      result.setResult(true);
    } catch (Exception e) {
      result.setNrErrors(1);
      e.printStackTrace();
      log.logError(toString(), "Error processing SendToS3Job : " + e.getMessage());
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }
}
