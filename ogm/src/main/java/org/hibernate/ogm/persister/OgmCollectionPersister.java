/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat, Inc. and/or its affiliates or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.ogm.persister;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.LoadQueryInfluencers;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.SubselectFetch;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.mapping.Collection;
import org.hibernate.ogm.jdbc.TupleAsMapResultSet;
import org.hibernate.ogm.loader.OgmBasicCollectionLoader;
import org.hibernate.ogm.metadata.GridMetadataManager;
import org.hibernate.ogm.metadata.GridMetadataManagerHelper;
import org.hibernate.ogm.type.GridType;
import org.hibernate.ogm.type.TypeTranslator;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.entity.Joinable;

/**
 * CollectionPersister storing the collection in a grid 
 *
 * @author Emmanuel Bernard
 */
public class OgmCollectionPersister extends AbstractCollectionPersister implements CollectionPhysicalModel {
	private static final Logger log = LoggerFactory.getLogger( OgmCollectionPersister.class );

	private final GridType keyGridType;
	private final GridType elementGridType;

	public OgmCollectionPersister(final Collection collection, final CollectionRegionAccessStrategy cacheAccessStrategy, final Configuration cfg, final SessionFactoryImplementor factory)
			throws MappingException, CacheException {
		super( collection, cacheAccessStrategy, cfg, factory );
		GridMetadataManager gridManager = GridMetadataManagerHelper.getGridMetadataManager( factory );
		final TypeTranslator typeTranslator = gridManager.getTypeTranslator();
		keyGridType = typeTranslator.getType( getKeyType() );
		elementGridType = typeTranslator.getType( getElementType() );

	}

	@Override
	public Object readKey(ResultSet rs, String[] aliases, SessionImplementor session)
	throws HibernateException, SQLException {
		final TupleAsMapResultSet resultset = rs.unwrap( TupleAsMapResultSet.class );
		final Map<String,Object> keyTuple = resultset.getTuple();
		return keyGridType.nullSafeGet( keyTuple, aliases, session, null );
	}

	@Override
	public Object readElement(ResultSet rs, Object owner, String[] aliases, SessionImplementor session)
	throws HibernateException, SQLException {
		final TupleAsMapResultSet resultset = rs.unwrap( TupleAsMapResultSet.class );
		final Map<String,Object> keyTuple = resultset.getTuple();
		return elementGridType.nullSafeGet( keyTuple, aliases, session, owner );
	}

	@Override
	protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	protected CollectionInitializer createCollectionInitializer(LoadQueryInfluencers loadQueryInfluencers)
			throws MappingException {
		//TODO pass constructor
		return new OgmBasicCollectionLoader(this);
	}

	@Override
	public boolean isOneToMany() {
		return false;
	}

	@Override
	public boolean isManyToMany() {
		//Let's see if we can model everything like that. That'd be nice
		return true;
	}

	@Override
	public boolean isCascadeDeleteEnabled() {
		//TODO always false: OGM does not assume cascade delete is supported by the underlying engine
		return false;
	}

	@Override
	protected String generateDeleteString() {
		return null;
	}

	@Override
	protected String generateDeleteRowString() {
		return null;
	}

	@Override
	protected String generateUpdateRowString() {
		return null;
	}

	@Override
	protected String generateInsertRowString() {
		return null;
	}

	@Override
	protected int doUpdateRows(Serializable key, PersistentCollection collection, SessionImplementor session)
			throws HibernateException {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String selectFragment(Joinable rhs, String rhsAlias, String lhsAlias, String currentEntitySuffix, String currentCollectionSuffix, boolean includeCollectionColumns) {
		return null;
	}

	@Override
	public String whereJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
		return null;
	}

	@Override
	public String fromJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
		return null;
	}

	@Override
	public boolean consumesEntityAlias() {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean consumesCollectionAlias() {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	protected void logStaticSQL() {
		if ( log.isDebugEnabled() ) {
			log.debug( "No SQL used when using OGM: " + getRole() );
		}
	}

	@Override
	public void postInstantiate() throws MappingException {
		//we don't have custom query loader, nothing to do
	}

	@Override
	protected CollectionInitializer getAppropriateInitializer(Serializable key, SessionImplementor session) {
		//we have no query loader
		//we don't handle subselect
		//we don't know how to support filters on OGM today
		return createCollectionInitializer( session.getLoadQueryInfluencers() );
	}
}
