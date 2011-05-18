/*
 * The MIT License
 *
 * Copyright (C) 2010-2011 by Anthony Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.publish_over_ssh;

import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.util.FormValidation;
import jenkins.plugins.publish_over.BPTransfer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class BapSshTransfer extends BapTransfer implements Describable<BapSshTransfer> {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public BapSshTransfer(final String sourceFiles, final String excludes, final String remoteDirectory, final String removePrefix,
                          final boolean remoteDirectorySDF, final boolean flatten, final String execCommand, final int execTimeout) {
        super(sourceFiles, excludes, remoteDirectory, removePrefix, remoteDirectorySDF, flatten, execCommand, execTimeout);
    }

    public DescriptorImpl getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(DescriptorImpl.class);
    }
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;

        return createEqualsBuilder((BapSshTransfer) that).isEquals();
    }

    public int hashCode() {
        return createHashCodeBuilder().toHashCode();
    }

    public String toString() {
        return addToToString(new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<BapSshTransfer> {
        @Override
        public String getDisplayName() {
            return Messages.transfer_descriptor_displayName();
        }
        public boolean canUseExcludes() {
            return BPTransfer.canUseExcludes();
        }
        public static int getDefaultExecTimeout() {
            return BapTransfer.DEFAULT_EXEC_TIMEOUT;
        }
        public FormValidation doCheckExecTimeout(@QueryParameter final String value) {
            return FormValidation.validateNonNegativeInteger(value);
        }
        public FormValidation doCheckSourceFiles(@QueryParameter final String configName, @QueryParameter final String sourceFiles,
                                                 @QueryParameter final String execCommand) {
            if (Util.fixEmptyAndTrim(configName) != null) {
                final BapSshPublisherPlugin.Descriptor pluginDescriptor = Hudson.getInstance().getDescriptorByType(
                                                                                                BapSshPublisherPlugin.Descriptor.class);
                final BapSshHostConfiguration hostConfig = pluginDescriptor.getConfiguration(configName);
                if (hostConfig == null)
                    return FormValidation.error(Messages.descriptor_sourceFiles_check_configNotFound(configName));
                if (hostConfig.isEffectiveDisableExec())
                    return FormValidation.validateRequired(sourceFiles);
            }
            return checkTransferSet(sourceFiles, execCommand);
        }
        public FormValidation doCheckExecCommand(@QueryParameter final String sourceFiles, @QueryParameter final String execCommand) {
            return checkTransferSet(sourceFiles, execCommand);
        }
        private FormValidation checkTransferSet(final String sourceFiles, final String execCommand) {
            return haveAtLeastOne(sourceFiles, execCommand) ? FormValidation.ok()
                : FormValidation.error(Messages.descriptor_sourceOrExec());
        }
        private boolean haveAtLeastOne(final String... values) {
            for (String value : values)
                if (Util.fixEmptyAndTrim(value) != null)
                    return true;
            return false;
        }
    }
}
