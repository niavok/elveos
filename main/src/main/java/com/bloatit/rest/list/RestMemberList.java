/*
 * Copyright (C) 2010 BloatIt.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BloatIt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.rest.list;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import com.bloatit.framework.utils.PageIterable;
import com.bloatit.model.Member;
import com.bloatit.rest.list.master.RestListBinder;
import com.bloatit.rest.resources.RestMember;

/**
 * <p>
 * Wraps a list of Member into a list of RestElements
 * </p>
 * <p>
 * This class can be represented in Xml as a list of Member<br />
 * Example:
 * 
 * <pre>
 * {@code <Members>}
 *     {@code <Member name=Member1 />}
 *     {@code <Member name=Member2 />}
 * {@code </Members>}
 * </pre>
 * <p>
 */
@XmlRootElement(name = "members")
public class RestMemberList extends RestListBinder<RestMember, Member> {

    /**
     * Provided for XML generation
     */
    @SuppressWarnings("unused")
    private RestMemberList() {
        super();
    }

    /**
     * Creates a RestMemberList from a {@codePageIterable<Member>}
     * 
     * @param collection the list of elements from the model
     */
    public RestMemberList(final PageIterable<Member> collection) {
        super(collection);
    }

    /**
     * This method is provided only to be able to represent the list as XmL
     */
    @XmlElement(name = "member")
    @XmlIDREF
    public List<RestMember> getMembers() {
        final List<RestMember> members = new ArrayList<RestMember>();
        for (final RestMember member : this) {
            members.add(member);
        }
        return members;
    }
}
