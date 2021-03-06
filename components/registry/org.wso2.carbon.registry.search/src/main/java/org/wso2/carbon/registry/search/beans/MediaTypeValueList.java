package org.wso2.carbon.registry.search.beans;

import edu.umd.cs.findbugs.annotations.*;

/**
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class MediaTypeValueList {
    String mediaType;
    String[] searchFields;

    public String[] getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(String[] searchFields) {
        this.searchFields = searchFields;
    }

    public String getMediaType() {

        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
