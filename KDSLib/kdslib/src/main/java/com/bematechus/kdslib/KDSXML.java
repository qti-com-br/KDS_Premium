/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;
//package pckds;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 *
 * @author David.Wong
 * parse XML file, 
 * 
 */
public class KDSXML {

    public static final String TAG = "KDSXML";
/************************************************************************/
/* 
 * read /write xml file, parse its data.
 * */
/************************************************************************/
        private Document m_doc=null;
        private Element m_current = null;
        private Element m_root = null;
        private Element m_saved = null;

        public KDSXML()
        {
            try
            {
                
                DocumentBuilder db=getDocBuilder();
                m_doc=db.newDocument();
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
            }
            
            
        }
        
        private void init()
        {
            //m_doc = new Document();

        }

        /************************************************************************/
        /* 
         * delete a attribute from the element
         * */
        /************************************************************************/
        public  boolean delAttribute(String name)
        {
            m_current.removeAttribute(name);
            return true;
        }
        /************************************************************************/
        /* get attribute value.
         * The caller get_attribute(name, ref value). //must has this "ref"
         * 
         * comments: 
         *  ref: parameters needs to assign initial value.
         *  out: no initial value, but it must been assign value in implemented function.
         * 
         * */
        /************************************************************************/
        public String getAttribute(String name, String defVal)
        {
            if (m_current.hasAttribute(name))
                return m_current.getAttribute(name);
            else
                return defVal;
            

        }
        /************************************************************************/
        /* 
         * create a new attribute in current element.
         * */
        /************************************************************************/
        public boolean newAttribute(String name, String value)
        {
            m_current.setAttribute(name, value);
            return true;
        }
        /************************************************************************/
        /* 
         * set new value to existed attribute.
         * */
        /************************************************************************/
        public boolean setAttribute(String name, String value)
        {
            m_current.setAttribute(name, value);
            return true;
        }
        /************************************************************************/
        /* find next group from current group. next sliding.
         * */
        /************************************************************************/
        public boolean getNextGroup(String name)
        {
            //Node node;
            //XmlNode element = m_current;
            Node node = null;
            Node element = m_current;
            do 
            {
                node = element.getNextSibling();
                if (node == null) return false;
                if (node.getNodeType() != Node.ELEMENT_NODE)
                {
                    element = node;
                }
                if (node.getNodeName().equals(name) )
                {
                    m_current = (Element)node;
                    return true;
                }
                element = node;
                //node = null;


            } while (node != null);
            return false;
            
        }
        public boolean delSubGroup(String name)
        {
            Node node = find_first_node(m_current, name);
            if (node == null) return true;
            m_current.removeChild(node);
            return true;
            
        }
        public boolean getFirstGroup(String name)
        {
            Node node = find_first_node(m_current, name);
            if (node == null) return false;
            if (node.getNodeType() != Node.ELEMENT_NODE) return false;
            m_current = (Element)node;
            return true;
        }
        
        public String getCurrentGroupValue()
        {
            return m_current.getTextContent();// .InnerText;
            
        }
        public String getSubGrouValue(String name, String strDefault)
        {
            Node node = find_first_node(m_current, name);
            if (node == null) return strDefault;
            if (node.getNodeType() != Node.ELEMENT_NODE) return strDefault;
            Element e = (Element)node;
            return e.getTextContent();
            
        }
        public boolean newGroup(String name, boolean bcurrent)
        {
            
            Node node = m_doc.createElement(name);
            if (m_current != null)
                node = m_current.appendChild(node) ;
            else
            {
                if (m_root != null)
                    node = m_root.appendChild(node);
                else
                {
                    if (m_doc != null)
                    {
                        m_root = (Element)(m_doc.appendChild(node));
                        m_current = m_root;
                        node = m_root;
                    }
                    else
                        return false;
                }
            }
            if (bcurrent)
                m_current = (Element)node;
            return true;
        }
        public boolean newGroup(String name, String value, boolean bcurrent)
        {
            Element element = m_doc.createElement(name);
            element.setTextContent(value);// .InnerText = value;
            Node node = m_current.appendChild(element);
            if (bcurrent)
                m_current = (Element)node;
            return true;
        }
        public boolean setGroupValue(String value)
        {
            
            if (m_current == null) return false;
            
           
            m_current.setTextContent(value);// .InnerText = value;
            return true;

        }
        public boolean setSubGroup(String name, String value)
        {
            
            if (m_current == null) return false;
             Node node = find_first_node(m_current, name);
            if (node == null) return false;
            if (node.getNodeType() != Node.ELEMENT_NODE) return false;
            Element e = (Element)node;
            e.setTextContent(value);           
            
            return true;

        }
        private DocumentBuilder getDocBuilder()
        {
            try
            {
                DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
                factory.setIgnoringElementContentWhitespace(true);
                DocumentBuilder db=factory.newDocumentBuilder();
                return db;
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
                return null;
            }
        }
        public boolean loadString(String txtXml)
        {

            //move to "<"
            int ncount = txtXml.length();
            for (int i=0; i< ncount; i++)
            {
                if (txtXml.charAt(0) == '<')
                {
                    break;
                }
                else
                {
                    txtXml = txtXml.substring(1);
                }
            }

            try
            {
                
                DocumentBuilder db=getDocBuilder();
                InputSource source = new InputSource(new StringReader(txtXml));
                m_doc = db.parse(source );
                //m_doc=db.parse(txtXml);//.parse new File("Test1.xml"));
                
                //root=xmldoc.getDocumentElement();
                
                //m_doc.LoadXml(txtXml);
                if (m_doc.getFirstChild().getNodeType() == Node.NOTATION_NODE)// XmlNodeType.XmlDeclaration)
                {
                    Node node = m_doc.getFirstChild();
                    m_root = (Element)node.getNextSibling();
                    
                }
                else
                {
                    m_root = (Element)(m_doc.getFirstChild() );
                }
                m_current = m_root;
            }
            catch (Exception ex)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() ,ex);//+ KDSLog.getStackTrace(ex));
                m_root = null;
                m_current = null;
                m_saved = null;
            }

            return true;

        }
        public String get_xml_string()
        {
            try
            {
                //XML转字符串
                TransformerFactory tf   =   TransformerFactory.newInstance();
                Transformer t = tf.newTransformer();

                t.setOutputProperty("encoding","utf-8");//解决中文问题，试过用GBK不行
                ByteArrayOutputStream bos   =   new ByteArrayOutputStream();
                t.transform(new DOMSource(m_doc), new StreamResult(bos));
                String xmlStr = bos.toString();
                return xmlStr;
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
                return "";
            }
            //return m_root.getTextContent();
            //return m_doc. .getDocumentURI();
            //return m_doc.getTextContent();// .InnerXml;

        }
        public String get_root_string()
        {
            if (m_root == null) return "";
            return m_root.getTextContent();// .InnerXml;
        }
        public boolean close()
        {

            //m_doc. .RemoveAll();
            m_doc = null;
            m_root = null;
            m_current = null;
            m_saved = null;
            return true;
        }
        public boolean new_doc_with_root(String strRoot)
        {

         
            Element element = this.m_doc.createElement(strRoot);

            Element node = (Element)(this.m_doc.appendChild(element));
            m_root = node;
            m_current = m_root;
            return true;

        }
        public boolean open_file(String filename, String rootname, boolean bcreate)
        {
            if (!KDSUtil.fileExisted(filename))
            {
                if (!bcreate) return false;
                String s = String.format("<%s></%s>", rootname, rootname);
                loadString(s);
                
                m_root = m_doc.getDocumentElement();
                m_current = m_root;
                m_saved = null;
            }
            else
            {

                try
                {
                    /*
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    //Document document = db.parse(fileName);
                    //DocumentBuilder db=getDocBuilder();
                    File f = new File(filename);
                    
                    m_doc=db.parse( f);
                    // m_doc=db.parse( filename);
                    //m_doc.Load(filename);
                    m_root = m_doc.getDocumentElement();
                    m_current = m_root;
                    m_saved = null;
                    */
                    String s = KDSUtil.readTextFile(filename, "utf-8");
                    loadString(s);
                    m_root = m_doc.getDocumentElement();
                    m_current = m_root;
                    m_saved = null;
                }
                catch (Exception ex)
                {
                    KDSLog.e(TAG,KDSLog._FUNCLINE_() ,ex);//+ KDSLog.getStackTrace(ex));
                    return false;
                }
                
            }
            return true;
        }
        public boolean reset()
        {
            m_doc = null;
            m_root = null;
            m_current = null;
            m_saved = null;
            return true;
        }
        public boolean write_file(String filename)
        {
            return saveXml(filename, m_doc);
            
//            XmlTextWriter tr = new XmlTextWriter(filename, null);
//            tr.Formatting = Formatting.Indented;
//            m_doc.WriteContentTo(tr);
//            tr.Close();
//            return true;
        }
         private boolean saveXml(String fileName, Document doc)
         {//将Document输出到文件
            TransformerFactory transFactory= TransformerFactory.newInstance();
            try {
                Transformer transformer = transFactory.newTransformer();
                transformer.setOutputProperty("indent", "yes");

                DOMSource source=new DOMSource();
                source.setNode(doc);
                StreamResult result=new StreamResult();
                result.setOutputStream(new FileOutputStream(fileName));

                transformer.transform(source, result);
                return true;
            } catch (TransformerConfigurationException e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
                //e.printStackTrace();
                return false;
            } catch (TransformerException e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
                //e.printStackTrace();
                return false;
            } catch (FileNotFoundException e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
                //e.printStackTrace();
                return false;
            }   
        }
        public boolean back_to_root()
        {
            m_current = m_doc.getDocumentElement();
            return true;
        }
        public boolean back_to_parent()
        {
            Node node = m_current.getParentNode();
            if (node == null)
                return false;
            if ((node.getNodeType() != Node.ELEMENT_NODE)) return false;
            m_current =(Element) node;

            return true;
        }
        public boolean save_current()
        {
            m_saved = m_current;
            return true;
        }
        public boolean restore_current()
        {
            m_current = m_saved;
            return true;
        }
        
        public String getCurrentName()
        {
            if (m_current == null)
                return "";
            return m_current.getNodeName();
        }
        /************************************************************************/
        /* find a child in given node
         * */
        /************************************************************************/
        private Node find_first_node(Node nodeparent, String name)
        {
            if (nodeparent == null) return null;
            Node node = nodeparent.getFirstChild();
            if (node == null) return null;
            
            do 
            {
                if (node.getNodeName().equals(name)) return node;
                node = node.getNextSibling();

                
            }while (node != null);

            return null;
        }
        /***********************************************************************
         * sliding to next node, 
         * @return 
         *      True:
         *          Next sliding node exsited.
         *      false:
         *          No next sliding node any more
         */
        public boolean slidingNext()
        {
            if (m_current == null)
                return false;
            
            Node node = m_current.getNextSibling();
            while ( (node != null) && (node.getNodeType() != Node.ELEMENT_NODE ) )
            {
                node = node.getNextSibling();
            }
            if (node != null &&(node.getNodeType() == Node.ELEMENT_NODE ) )
            {
                m_current = (Element)node;
                return true;
            }
            return false;
            
        }
        public boolean moveToFirstChild()
        {
             if (m_current == null)
                return false;
            
            Node node = m_current.getFirstChild();
           // System.out.println( m_current.getNodeName());
            
            if (node.getNodeType() != Node.ELEMENT_NODE )
            {
                while ( (node != null) && (node.getNodeType() != Node.ELEMENT_NODE ) )
                {
                    node = node.getNextSibling();
                }
                //System.out.println("not node");
            }
//            System.out.println( node.getNodeName());
//            System.out.println( node.getTextContent());
            
            
            if (node != null && (node.getNodeType() == Node.ELEMENT_NODE ))
            {
                 m_current = (Element)node;
                return true; 
            }
            return false;
        }

       

}
