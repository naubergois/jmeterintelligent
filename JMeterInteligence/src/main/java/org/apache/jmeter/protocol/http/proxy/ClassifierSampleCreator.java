package org.apache.jmeter.protocol.http.proxy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.config.MultipartUrlConfig;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseClassifier;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactoryClassifier;
import org.apache.jmeter.protocol.http.sampler.PostWriter;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ClassifierSampleCreator extends AbstractSamplerCreatorClassifier {

	private static final Logger log = LoggingManager.getLoggerForClass();

	/**
	     * 
	     */

	public String[] getManagedContentTypes() {
		return new String[0];
	}

	/**
	 * 
	 * @see org.apache.jmeter.protocol.http.proxy.SamplerCreator#createSampler(org.apache.jmeter.protocol.http.proxy.HttpRequestHdr,
	 *      java.util.Map, java.util.Map)
	 */

	public HTTPSamplerBaseClassifier createSampler(
			HttpRequestHdrClassifier request,
			Map<String, String> pageEncodings, Map<String, String> formEncodings) {
		// Instantiate the sampler
		HTTPSamplerBaseClassifier sampler = HTTPSamplerFactoryClassifier
				.newInstance(request.getHttpSamplerName());

		sampler.setProperty(TestElement.GUI_CLASS,
				HttpTestSampleGui.class.getName());

		// Defaults
		sampler.setFollowRedirects(false);
		sampler.setUseKeepAlive(true);

		if (log.isDebugEnabled()) {
			log.debug("getSampler: sampler path = " + sampler.getPath());
		}
		return sampler;
	}

	/**
	 * @see org.apache.jmeter.protocol.http.proxy.SamplerCreator#populateSampler(org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase,
	 *      org.apache.jmeter.protocol.http.proxy.HttpRequestHdr, java.util.Map,
	 *      java.util.Map)
	 */

	public final void populateSampler(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request,
			Map<String, String> pageEncodings, Map<String, String> formEncodings)
			throws Exception {
		computeFromHeader(sampler, request, pageEncodings, formEncodings);

		computeFromPostBody(sampler, request);
		if (log.isDebugEnabled()) {
			log.debug("sampler path = " + sampler.getPath());
		}
		Arguments arguments = sampler.getArguments();
		if (arguments.getArgumentCount() == 1
				&& arguments.getArgument(0).getName().length() == 0) {
			sampler.setPostBodyRaw(true);
		}

	}

	/**
	 * Compute sampler informations from Request Header
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 * @param pageEncodings
	 *            Map<String, String>
	 * @param formEncodings
	 *            Map<String, String>
	 * @throws Exception
	 */
	protected void computeFromHeader(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request,
			Map<String, String> pageEncodings, Map<String, String> formEncodings)
			throws Exception {
		computeDomain(sampler, request);

		computeMethod(sampler, request);

		computePort(sampler, request);

		computeProtocol(sampler, request);

		computeContentEncoding(sampler, request, pageEncodings, formEncodings);

		computePath(sampler, request);

		computeSamplerName(sampler, request);
	}

	/**
	 * Compute sampler informations from Request Header
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 * @throws Exception
	 */
	protected void computeFromPostBody(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request) throws Exception {
		// If it was a HTTP GET request, then all parameters in the URL
		// has been handled by the sampler.setPath above, so we just need
		// to do parse the rest of the request if it is not a GET request
		if ((!HTTPConstants.CONNECT.equals(request.getMethod()))
				&& (!HTTPConstants.GET.equals(request.getMethod()))) {
			// Check if it was a multipart http post request
			final String contentType = request.getContentType();
			MultipartUrlConfig urlConfig = request
					.getMultipartConfig(contentType);
			String contentEncoding = sampler.getContentEncoding();
			// Get the post data using the content encoding of the request
			String postData = null;
			if (log.isDebugEnabled()) {
				if (!StringUtils.isEmpty(contentEncoding)) {
					log.debug("Using encoding " + contentEncoding
							+ " for request body");
				} else {
					log.debug("No encoding found, using JRE default encoding for request body");
				}
			}

			if (!StringUtils.isEmpty(contentEncoding)) {
				postData = new String(request.getRawPostData(), contentEncoding);
			} else {
				// Use default encoding
				postData = new String(request.getRawPostData(),
						PostWriter.ENCODING);
			}

			if (urlConfig != null) {
				urlConfig.parseArguments(postData);
				// Tell the sampler to do a multipart post
				sampler.setDoMultipartPost(true);
				// Remove the header for content-type and content-length, since
				// those values will most likely be incorrect when the sampler
				// performs the multipart request, because the boundary string
				// will change
				request.getHeaderManager().removeHeaderNamed(
						HttpRequestHdr.CONTENT_TYPE);
				request.getHeaderManager().removeHeaderNamed(
						HttpRequestHdr.CONTENT_LENGTH);

				// Set the form data
				sampler.setArguments(urlConfig.getArguments());
				// Set the file uploads
				sampler.setHTTPFiles(urlConfig.getHTTPFileArgs().asArray());
				// used when postData is pure xml (eg. an xml-rpc call) or for
				// PUT
			} else if (postData.trim().startsWith("<?")
					|| HTTPConstants.PUT.equals(sampler.getMethod())
					|| isPotentialXml(postData)) {
				sampler.addNonEncodedArgument("", postData, "");
			} else if (contentType == null
					|| (contentType
							.startsWith(HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED) && !isBinaryContent(contentType))) {
				// It is the most common post request, with parameter name and
				// values
				// We also assume this if no content type is present, to be most
				// backwards compatible,
				// but maybe we should only parse arguments if the content type
				// is as expected
				sampler.parseArguments(postData.trim(), contentEncoding); // standard
																			// name=value
																			// postData
			} else if (postData.length() > 0) {
				if (isBinaryContent(contentType)) {
					try {
						File tempDir = new File(getBinaryDirectory());
						File out = File.createTempFile(request.getMethod(),
								getBinaryFileSuffix(), tempDir);
						FileUtils.writeByteArrayToFile(out,
								request.getRawPostData());
						HTTPFileArg[] files = { new HTTPFileArg(out.getPath(),
								"", contentType) };
						sampler.setHTTPFiles(files);
					} catch (IOException e) {
						log.warn("Could not create binary file: " + e);
					}
				} else {
					// Just put the whole postbody as the value of a parameter
					sampler.addNonEncodedArgument("", postData, ""); // used
																		// when
																		// postData
																		// is
																		// pure
																		// xml
																		// (ex.
																		// an
																		// xml-rpc
																		// call)
				}
			}
		}
	}

	/**
	 * Tries parsing to see if content is xml
	 * 
	 * @param postData
	 *            String
	 * @return boolean
	 */
	private static final boolean isPotentialXml(String postData) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			ErrorDetectionHandler detectionHandler = new ErrorDetectionHandler();
			xmlReader.setContentHandler(detectionHandler);
			xmlReader.setErrorHandler(detectionHandler);
			xmlReader.parse(new InputSource(new StringReader(postData)));
			return !detectionHandler.isErrorDetected();
		} catch (ParserConfigurationException e) {
			return false;
		} catch (SAXException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private static final class ErrorDetectionHandler extends DefaultHandler {
		private boolean errorDetected = false;

		public ErrorDetectionHandler() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException
		 * )
		 */

		public void error(SAXParseException e) throws SAXException {
			this.errorDetected = true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.
		 * SAXParseException)
		 */

		public void fatalError(SAXParseException e) throws SAXException {
			this.errorDetected = true;
		}

		/**
		 * @return the errorDetected
		 */
		public boolean isErrorDetected() {
			return errorDetected;
		}
	}

	/**
	 * Compute sampler name
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 */
	protected void computeSamplerName(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request) {
		if (!HTTPConstants.CONNECT.equals(request.getMethod())
				&& isNumberRequests()) {
			incrementRequestNumber();
			sampler.setName(getRequestNumber() + " " + sampler.getPath());
		} else {
			sampler.setName(sampler.getPath());
		}
	}

	/**
	 * Set path on sampler
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 */
	protected void computePath(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request) {
		if (sampler.getContentEncoding() != null) {
			sampler.setPath(request.getPath(), sampler.getContentEncoding());
		} else {
			// Although the spec says UTF-8 should be used for encoding URL
			// parameters,
			// most browser use ISO-8859-1 for default if encoding is not known.
			// We use null for contentEncoding, then the url parameters will be
			// added
			// with the value in the URL, and the "encode?" flag set to false
			sampler.setPath(request.getPath(), null);
		}
		if (log.isDebugEnabled()) {
			log.debug("Proxy: setting path: " + sampler.getPath());
		}
	}

	/**
	 * Compute content encoding
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 * @param pageEncodings
	 *            Map<String, String>
	 * @param formEncodings
	 *            Map<String, String>
	 * @throws MalformedURLException
	 */
	protected void computeContentEncoding(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request,
			Map<String, String> pageEncodings, Map<String, String> formEncodings)
			throws MalformedURLException {
		URL pageUrl = null;
		if (sampler.isProtocolDefaultPort()) {
			pageUrl = new URL(sampler.getProtocol(), sampler.getDomain(),
					request.getPath());
		} else {
			pageUrl = new URL(sampler.getProtocol(), sampler.getDomain(),
					sampler.getPort(), request.getPath());
		}
		String urlWithoutQuery = request.getUrlWithoutQuery(pageUrl);

		String contentEncoding = computeContentEncoding(request, pageEncodings,
				formEncodings, urlWithoutQuery);

		// Set the content encoding
		if (!StringUtils.isEmpty(contentEncoding)) {
			sampler.setContentEncoding(contentEncoding);
		}
	}

	/**
	 * Computes content encoding from request and if not found uses pageEncoding
	 * and formEncoding to see if URL was previously computed with a content
	 * type
	 * 
	 * @param request
	 *            {@link HttpRequestHdr}
	 * @param pageEncodings
	 *            Map<String, String>
	 * @param formEncodings
	 *            Map<String, String>
	 * @return String content encoding
	 */
	protected String computeContentEncoding(HttpRequestHdrClassifier request,
			Map<String, String> pageEncodings,
			Map<String, String> formEncodings, String urlWithoutQuery) {
		// Check if the request itself tells us what the encoding is
		String contentEncoding = null;
		String requestContentEncoding = ConversionUtils
				.getEncodingFromContentType(request.getContentType());
		if (requestContentEncoding != null) {
			contentEncoding = requestContentEncoding;
		} else {
			// Check if we know the encoding of the page
			if (pageEncodings != null) {
				synchronized (pageEncodings) {
					contentEncoding = pageEncodings.get(urlWithoutQuery);
				}
			}
			// Check if we know the encoding of the form
			if (formEncodings != null) {
				synchronized (formEncodings) {
					String formEncoding = formEncodings.get(urlWithoutQuery);
					// Form encoding has priority over page encoding
					if (formEncoding != null) {
						contentEncoding = formEncoding;
					}
				}
			}
		}
		return contentEncoding;
	}

	/**
	 * Set protocol on sampler
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 */
	protected void computeProtocol(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request) {
		sampler.setProtocol(request.getProtocol(sampler));
	}

	/**
	 * Set Port on sampler
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 */
	protected void computePort(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request) {
		sampler.setPort(request.serverPort());
		if (log.isDebugEnabled()) {
			log.debug("Proxy: setting port: " + sampler.getPort());
		}
	}

	/**
	 * Set method on sampler
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 */
	protected void computeMethod(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request) {
		sampler.setMethod(request.getMethod());
		log.debug("Proxy: setting method: " + sampler.getMethod());
	}

	/**
	 * Set domain on sampler
	 * 
	 * @param sampler
	 *            {@link HTTPSamplerBase}
	 * @param request
	 *            {@link HttpRequestHdr}
	 */
	protected void computeDomain(HTTPSamplerBaseClassifier sampler,
			HttpRequestHdrClassifier request) {
		sampler.setDomain(request.serverName());
		if (log.isDebugEnabled()) {
			log.debug("Proxy: setting server: " + sampler.getDomain());
		}
	}

}
