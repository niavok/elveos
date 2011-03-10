//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.model.right;

import com.bloatit.model.Software;

/**
 * The Class SoftwareRight store the properties accessor for the {@link Software}
 * class.
 */
public class SoftwareRight extends RightManager {

    /**
     * The Class TeamList is an accessor for the TeamList property.
     */
    public static class TeamList extends Accessor {

        /*
         * (non-Javadoc)
         * @see com.bloatit.model.right.Accessor#can(com.bloatit.model.right.
         * RestrictedInterface , com.bloatit.model.right.Action)
         */
        @Override
        protected final boolean can(final RestrictedInterface role, final Action action) {
            boolean can = false;
            can = can || canRead(action);
            can = can || ownerCanWrite(role, action);
            can = can || ownerCanDelete(role, action);
            return can;
        }
    }

    /**
     * The Class Name is a {@link Public} accessor for the Name property.
     */
    public static class Name extends Public {
        // nothing this is just a rename.
    }
}
