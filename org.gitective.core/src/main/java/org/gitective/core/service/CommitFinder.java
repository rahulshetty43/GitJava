/******************************************************************************
 *  Copyright (c) 2011, Kevin Sawicki <kevinsawicki@gmail.com>
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/
package org.gitective.core.service;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.gitective.core.Assert;
import org.gitective.core.CommitUtils;
import org.gitective.core.GitException;

/**
 * Commit locator class
 */
public class CommitFinder extends RepositoryService {

	/**
	 * Filter for selecting commits to match
	 */
	protected RevFilter preFilter;

	/**
	 * Tree filter
	 */
	protected TreeFilter treeFilter;

	/**
	 * Filter for matches
	 */
	protected RevFilter postFilter;

	/**
	 * @param gitDirs
	 */
	public CommitFinder(File... gitDirs) {
		super(gitDirs);
	}

	/**
	 * @param repositories
	 */
	public CommitFinder(Repository... repositories) {
		super(repositories);
	}

	/**
	 * @param gitDirs
	 */
	public CommitFinder(String... gitDirs) {
		super(gitDirs);
	}

	/**
	 * Set the {@link RevFilter} to use to filter commits during searches.
	 * 
	 * @param filter
	 * @return this service
	 */
	public CommitFinder setFilter(RevFilter filter) {
		preFilter = filter;
		return this;
	}

	/**
	 * Set the {@link RevFilter} to use to match filtered commits.
	 * 
	 * @param filter
	 * @return this service
	 */
	public CommitFinder setMatcher(RevFilter filter) {
		postFilter = filter;
		return this;
	}

	/**
	 * Set the {@link TreeFilter} to use to limit commits visited.
	 * 
	 * @param filter
	 * @return this service
	 */
	public CommitFinder setFilter(TreeFilter filter) {
		treeFilter = filter;
		return this;
	}

	/**
	 * Walk commits between the start commit id and end commit id. Starting
	 * commit and filter cannot be null.
	 * 
	 * @param repository
	 * @param start
	 * @param end
	 * @return this service
	 */
	protected CommitFinder walk(Repository repository, ObjectId start,
			ObjectId end) {
		Assert.notNull("Starting commit id cannot be null", start);
		final RevWalk walk = new RevWalk(repository);
		walk.setRetainBody(true);
		walk.setRevFilter(preFilter);
		walk.setTreeFilter(treeFilter);
		try {
			walk.markStart(walk.parseCommit(start));
			if (end != null)
				walk.markUninteresting(walk.parseCommit(end));
			if (postFilter != null) {
				RevCommit commit;
				while ((commit = walk.next()) != null)
					if (!postFilter.include(walk, commit))
						return this;
			} else
				while (walk.next() != null)
					;
		} catch (IOException e) {
			throw new GitException(e);
		} catch (StopWalkException ignored) {
			// Ignored
		} finally {
			walk.release();
		}
		return this;
	}

	/**
	 * Search commits starting from the given commit id.
	 * 
	 * @param start
	 * @return this service
	 */
	public CommitFinder findFrom(ObjectId start) {
		findBetween(start, (ObjectId) null);
		return this;
	}

	/**
	 * Search commits starting from the given revision string.
	 * 
	 * @param start
	 * @return this service
	 */
	public CommitFinder findFrom(String start) {
		return findBetween(start, (ObjectId) null);
	}

	/**
	 * Search commits starting at the commit that {@link Constants#HEAD}
	 * currently points to.
	 * 
	 * @return this service
	 */
	public CommitFinder find() {
		return findFrom(Constants.HEAD);
	}

	/**
	 * Search commits between the given start and end commits.
	 * 
	 * @param start
	 * @param end
	 * @return this service
	 */
	public CommitFinder findBetween(ObjectId start, ObjectId end) {
		final Repository[] repos = repositories;
		final int repoCount = repositories.length;
		for (int i = 0; i < repoCount; i++)
			walk(repos[i], start, end);
		return this;
	}

	/**
	 * Search commits between the given start revision string and the given end
	 * commit id.
	 * 
	 * @param start
	 * @param end
	 * @return this service
	 */
	public CommitFinder findBetween(String start, ObjectId end) {
		final Repository[] repos = repositories;
		final int repoCount = repositories.length;
		Repository repo;
		for (int i = 0; i < repoCount; i++) {
			repo = repos[i];
			walk(repo, CommitUtils.getCommit(repo, start), end);
		}
		return this;
	}

	/**
	 * Search commits between the given start commit id and the given end
	 * revision string.
	 * 
	 * @param start
	 * @param end
	 * @return this service
	 */
	public CommitFinder findBetween(ObjectId start, String end) {
		final Repository[] repos = repositories;
		final int repoCount = repositories.length;
		Repository repo;
		for (int i = 0; i < repoCount; i++) {
			repo = repos[i];
			walk(repo, start, CommitUtils.getCommit(repo, end));
		}
		return this;
	}

	/**
	 * Search commits between the given start revision string and the given end
	 * revision string.
	 * 
	 * @param start
	 * @param end
	 * @return this service
	 */
	public CommitFinder findBetween(String start, String end) {
		final Repository[] repos = repositories;
		final int repoCount = repositories.length;
		Repository repo;
		for (int i = 0; i < repoCount; i++) {
			repo = repos[i];
			walk(repo, CommitUtils.getCommit(repo, start),
					CommitUtils.getCommit(repo, end));
		}
		return this;
	}
}
