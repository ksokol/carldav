package org.unitedinternet.cosmo.model.hibernate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Kamill Sokol
 */
@Entity
@DiscriminatorValue("calendarcollection")
public class HibCalendarCollectionItem extends HibCollectionItem {}
