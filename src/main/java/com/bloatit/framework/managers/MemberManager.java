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
package com.bloatit.framework.managers;

import com.bloatit.common.PageIterable;
import com.bloatit.framework.Member;
import com.bloatit.framework.lists.MemberList;
import com.bloatit.model.data.DBRequests;
import com.bloatit.model.data.DaoMember;
import java.util.Iterator;
import java.util.List;

public class MemberManager {

    public static Member getMemberByLogin(String login) {
        return new Member(DaoMember.getByLogin(login));
    }

    public static boolean existsMember(String login) {
        return DaoMember.exist(login);
    }

    public static Member getMemberById(Integer id) {
        return new Member(DBRequests.getById(DaoMember.class, id));
    }

    public static PageIterable<Member> getMembers() {
        return new MemberList(DBRequests.getAll(DaoMember.class));
    }

}
