package com.patson.util

import java.util.concurrent.TimeUnit

import com.patson.data.AllianceSource
import com.patson.model._


object AllianceCache {

  import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}

  val detailedCache: LoadingCache[Int, Option[Alliance]] = CacheBuilder.newBuilder.maximumSize(1000).expireAfterAccess(2, TimeUnit.MINUTES).build(new DetailedLoader())
  val simpleCache: LoadingCache[Int, Option[Alliance]] = CacheBuilder.newBuilder.maximumSize(1000).expireAfterAccess(2, TimeUnit.MINUTES).build(new SimpleLoader())

  def getAlliance(allianceId : Int, fullLoad : Boolean = false) : Option[Alliance] = {
    if (fullLoad) {
      detailedCache.get(allianceId)
    } else {
      simpleCache.get(allianceId)
    }
  }

  def isEstablishedAndValid(allianceId: Int, airlineId: Int) : Boolean = {
    val alliance = getAlliance(allianceId).get
    alliance.status == AllianceStatus.ESTABLISHED &&
      alliance.members.exists(member => member.airline.id == airlineId && member.role != AllianceRole.APPLICANT)
  }

  def invalidateAlliance(allianceId : Int) = {
    detailedCache.invalidate(allianceId)
    simpleCache.invalidate(allianceId)
  }

  def invalidateAll() = {
    detailedCache.invalidateAll()
    simpleCache.invalidateAll()
  }

  class DetailedLoader extends CacheLoader[Int, Option[Alliance]] {
    override def load(allianceId: Int) = {
      AllianceSource.loadAllianceById(allianceId, true)
    }
  }

  class SimpleLoader extends CacheLoader[Int, Option[Alliance]] {
    override def load(allianceId: Int) = {
      AllianceSource.loadAllianceById(allianceId, false)
    }
  }


}



