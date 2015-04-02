package com.adaptris.core.varsub;

import static com.adaptris.core.varsub.Constants.DEFAULT_VARIABLE_POSTFIX;
import static com.adaptris.core.varsub.Constants.DEFAULT_VARIABLE_PREFIX;
import static com.adaptris.core.varsub.Constants.DEFAULT_VAR_SUB_IMPL;
import static com.adaptris.core.varsub.Constants.VARSUB_POSTFIX_KEY;
import static com.adaptris.core.varsub.Constants.VARSUB_PREFIX_KEY;
import static com.adaptris.core.varsub.Constants.VARSUB_IMPL_KEY;
import static com.adaptris.core.varsub.Constants.VARSUB_ADDITIONAL_LOGGING;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;

class Processor {

  private Properties cfg = new Properties();

  public Processor(Properties configuration) {
    cfg = configuration;
  }

  String process(String xml, Properties variables) throws CoreException {
    String varSubImpl = defaultIfBlank(cfg.getProperty(VARSUB_IMPL_KEY), DEFAULT_VAR_SUB_IMPL);
    String variablePrefix = defaultIfBlank(cfg.getProperty(VARSUB_PREFIX_KEY), DEFAULT_VARIABLE_PREFIX);
    String variablePostfix = defaultIfBlank(cfg.getProperty(VARSUB_POSTFIX_KEY), DEFAULT_VARIABLE_POSTFIX);
    boolean logIt = BooleanUtils.toBoolean(defaultIfBlank(cfg.getProperty(VARSUB_ADDITIONAL_LOGGING), "false"));
    Properties expandedVariables = new VariableExpander(variablePrefix, variablePostfix).resolve(variables);

    VariableSubstitutionImplFactory impl = VariableSubstitutionImplFactory.valueOf(varSubImpl);
    return impl.create().doSubstitution(xml, expandedVariables, variablePrefix, variablePostfix, logIt);
  }

  String process(URL urlToXml, Properties variables) throws CoreException {
    String result = null;
    try (InputStream inputStream = urlToXml.openConnection().getInputStream()) {
      String xml = IOUtils.toString(inputStream);
      result = this.process(xml, variables);
    }
    catch (IOException ex) {
      ExceptionHelper.rethrowCoreException(ex);
    }
    return result;
  }

}