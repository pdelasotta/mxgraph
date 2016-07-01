package cl.riescorp.drawioExporter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.io.OutputStream;
//import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.mxgraph.canvas.mxGraphicsCanvas2D;
import com.mxgraph.canvas.mxICanvas2D;
import com.mxgraph.reader.mxSaxOutputHandler;

class DrawioExporter {
	protected static transient Hashtable<String, Image> imageCache = new Hashtable<String, Image>();
	private static transient SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	protected static void writePdf(String url, String fname, int w, int h, Color bg, String xml)
			throws DocumentException, IOException, SAXException, ParserConfigurationException
	{
//		response.setContentType("application/pdf");

//		if (fname != null)
//		{
//			response.setHeader("Content-Disposition", "attachment; filename=\"" + fname + "\"; filename*=UTF-8''" + fname);
//		}

		// Fixes PDF offset
		w += 1;
		h += 1;

		Document document = new Document(new Rectangle(w, h));
		
		OutputStream outputStream = null;
		//TODO: inicializar correctamente el outputstream.
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		document.open();

		mxGraphicsCanvas2D gc = createCanvas(/*url,*/ writer.getDirectContent().createGraphics(w, h));

		// Fixes PDF offset
		gc.translate(1, 1);

		renderXml(xml, gc);
		gc.getGraphics().dispose();
		document.close();
		writer.flush();
		writer.close();
	}
	protected static void renderXml(String xml, mxICanvas2D canvas) throws SAXException, ParserConfigurationException, IOException
	{
		XMLReader reader = parserFactory.newSAXParser().getXMLReader();
		reader.setContentHandler(new mxSaxOutputHandler(canvas));
		reader.parse(new InputSource(new StringReader(xml)));
	}
	protected static mxGraphicsCanvas2D createCanvas(/*String url,*/ Graphics2D g2)
	{
		// Caches custom images for the time of the request
		final Hashtable<String, Image> shortCache = new Hashtable<String, Image>();
//		final String domain = url.substring(0, url.lastIndexOf("/"));

		mxGraphicsCanvas2D g2c = new mxGraphicsCanvas2D(g2)
		{
			public Image loadImage(String src)
			{
				// Uses local image cache by default
				Hashtable<String, Image> cache = shortCache;

//				// Uses global image cache for local images
//				if (src.startsWith(domain))
//				{
//					cache = imageCache;
//				}

				Image image = cache.get(src);

				if (image == null)
				{
					image = super.loadImage(src);

					if (image != null)
					{
						cache.put(src, image);
					}
					else
					{
						cache.put(src, EMPTY_IMAGE);
					}
				}
				else if (image == EMPTY_IMAGE)
				{
					image = null;
				}

				return image;
			}
		};

		return g2c;
	}
	public static BufferedImage EMPTY_IMAGE;

	/**
	 * Initializes the empty image.
	 */
	static
	{
		try
		{
			EMPTY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
		catch (Exception e)
		{
			// ignore
		}
	}
}
