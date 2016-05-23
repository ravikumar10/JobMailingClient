package Mailer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Mailer {
	
	public String userName=null;
	public String userId=null;
	public String mailMessage=null;
	public 	String password=null;
	public String resumePath=null;
	public String subject=null;
	public String csvFile=null;
	public String currentCompany=null;
	
	public Mailer() {}
	public static void main(String[] args)	{
	
	Mailer sendMail=new Mailer();	
    String XmlFile = null;    
    for(int i=0;i<args.length;i++)
    {
        String arg = args[i];
        if(arg.equalsIgnoreCase("-csv"))
        {
            if((args.length-i)>0)
            {
            	XmlFile = args[++i];              
            }
        }    
    }    
	sendMail.ReadXMLFile(XmlFile);	
	boolean success=sendMail.sendMail();
	System.out.println("\n================================================\n");
	if(success)
		System.out.println("All Mail Have Sent Successfully");
	else
		System.out.println("Process Failed");
	System.out.println("\n================================================\n");
   }
	public  void ReadXMLFile(String XmlFile) {
		try {
			
			File fXmlFile = new File(XmlFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("credentials");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);								
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;					
					userId=eElement.getElementsByTagName("userid").item(0).getTextContent();
					password=eElement.getElementsByTagName("password").item(0).getTextContent();
					userName=eElement.getElementsByTagName("name").item(0).getTextContent();
					mailMessage=eElement.getElementsByTagName("message").item(0).getTextContent();
					resumePath=eElement.getElementsByTagName("attachement").item(0).getTextContent();
					subject=eElement.getElementsByTagName("subject").item(0).getTextContent();
					csvFile=eElement.getElementsByTagName("csvFile").item(0).getTextContent();
					currentCompany=eElement.getElementsByTagName("currentCompany").item(0).getTextContent().trim().toLowerCase();
					
				}
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		
	}
	public boolean sendMail(){
		 String line = "";
		 String cvsSplitBy = ",";
		BufferedReader br = null;
		int count=1;
		try {

		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {
			StringBuilder sb=new StringBuilder();
			//Set property
			Properties props = System.getProperties();
		    props.put("mail.smtp.starttls.enable", true); // added this line
		    props.put("mail.smtp.host", "smtp.gmail.com");	    
		    props.put("mail.smtp.port", "587");
		    props.put("mail.smtp.auth", true);
		    props.put("mail.smtp.reportsuccess","true");	   
		    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
		      	protected PasswordAuthentication getPasswordAuthentication() {
		            return new PasswordAuthentication(userId, password);
		        }
		    });
		    MimeMessage message = new MimeMessage(session);
		 	String[] contact = line.split(cvsSplitBy);
		 	if(contact.length>=31){
		 	if(contact[29].trim().toLowerCase().contains(currentCompany) || contact[31].trim().toLowerCase().contains(currentCompany)||contact[29].trim().toLowerCase().contains(currentCompany) || contact[31].trim().toLowerCase().contains(currentCompany)){
		 		//System.out.println("found mitel Employe :  "+ contact[1]+" Last Name : "+ contact[3]);
		 		continue;
		 		
		 	}
		 	else if(contact[1]!=null&&contact[5]!=null&&contact[31]!=null&&contact[31].trim().toLowerCase().contains("talent") ||contact[31].trim().toLowerCase().contains("acquisition")||contact[31].trim().toLowerCase().contains("hiring")||contact[31].trim().toLowerCase().contains("recruiter") && !contact[31].trim().toLowerCase().contains("campus")){
			 		//sb.append("Contact Details [First Name = " + contact[1] + " ,Last name =" + contact[3] +" ,Email ID =" + contact[5] + "]\n");
		 			String to =contact[5].substring(1, contact[5].length()-1);
		 		 	System.out.println("COUNT NO.: "+count+"\t :: Sending Mail To : "+to +" \t::\tCompany : "+contact[29].substring(1, contact[29].length()-1)+ "\t::\tprofile : "+contact[31].substring(1, contact[31].length()-1) );
		 		 	count+=1;
		 		 	
					sb.append("Hi "+ contact[1].substring(1, contact[1].length()-1)+",\n\n");
					sb.append(mailMessage+"\n\n");
					sb.append("Thanks & Regards,\n"+userName);				
				    try {
				        InternetAddress from = new InternetAddress("sitravi.kumar@gmail.com");
				        message.setSubject(subject);
				        message.setFrom(from);
				      
				        	    
				        message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
	
				        // Create a multi-part to combine the parts
				        Multipart multipart = new MimeMultipart("alternative");
	
				        // Create your text message part
				        BodyPart messageBodyPart = new MimeBodyPart();
				        messageBodyPart.setText(sb.toString());
	
				        // Add the text part to the multipart
				        multipart.addBodyPart(messageBodyPart);
	
				        // Create the html part    
	
	
				        // Add html part to multi part
				        multipart.addBodyPart(messageBodyPart);
				        //add Resume
				        if(resumePath!=null){
				        	MimeBodyPart attachResume = new MimeBodyPart();					    
				        	attachResume.attachFile(resumePath);				   
				        	multipart.addBodyPart(attachResume);
				        }
				        // Associate multi-part with message
				        message.setContent(multipart);
	
				        // Send message
				       Transport transport = session.getTransport("smtp");
				       transport.connect("smtp.gmail.com", userId, password);			       
				       transport.send(message, message.getAllRecipients());
	
	
				    } catch (AddressException e) {
				        // TODO Auto-generated catch block
				       // e.printStackTrace();
				    } catch (MessagingException e) {
				        // TODO Auto-generated catch block
				       // e.printStackTrace();
				    }
				}
		 	   }
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
		
	}

}