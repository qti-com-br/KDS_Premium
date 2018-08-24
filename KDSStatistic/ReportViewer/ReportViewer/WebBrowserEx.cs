using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Windows.Forms;

using System.Reflection;
using System.Xml;
using System.IO;
using System.Xml.Xsl;

namespace ReportViewer
{
    class WebBrowserEx : WebBrowser
    {
        private string m_xsltFile = "defaultss.xslt";
        public XmlDocument DocumentXml
        {
            set
            {
                Assembly asmb = System.Reflection.Assembly.GetExecutingAssembly();
                Stream s = asmb.GetManifestResourceStream(asmb.GetName().Name + "." + m_xsltFile);
                XmlReader xr = XmlReader.Create(s);
                XslCompiledTransform xct = new XslCompiledTransform();
                xct.Load(xr);

                StringBuilder sb = new StringBuilder();
                XmlWriter xw = XmlWriter.Create(sb);
                xct.Transform(value, xw);

                this.DocumentText = sb.ToString();
            }
        }

        public string XmlStyleTranferFile
        {
            set { m_xsltFile = value; }
        }
    }
}
