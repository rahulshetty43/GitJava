/******************************************************************************
 *  Copyright (c) 2011, Kevin Sawicki <kevinsawicki@gmail.com>
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/
package org.gitective.tests;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.gitective.core.filter.commit.AllCommitFilter;
import org.gitective.core.filter.commit.CommitFilter;
import org.gitective.core.service.CommitService;

/**
 * Unit tests of {@link AllCommitFilter}
 */
public class AllTest extends GitTestCase {

	/**
	 * Test always matching despite child filter not-including
	 * 
	 * @throws Exception
	 */
	public void testMatch() throws Exception {
		final RevCommit[] visited = new RevCommit[] { null };
		CommitFilter filter = new CommitFilter() {

			public boolean include(RevWalk walker, RevCommit cmit)
					throws StopWalkException, MissingObjectException,
					IncorrectObjectTypeException, IOException {
				visited[0] = cmit;
				return false;
			}

			public RevFilter clone() {
				return this;
			}
		};
		RevCommit commit = add("file.txt", "content");
		CommitService service = new CommitService(testRepo);
		service.search(new AllCommitFilter().add(filter));
		assertEquals(commit, visited[0]);
	}
}