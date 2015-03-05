package com.eigengo.lift.common

import com.eigengo.lift.common.UserId
import spray.routing._
import spray.routing.directives.PathDirectives

trait CommonPathDirectives extends PathDirectives {
  val UserIdValue: PathMatcher1[UserId] = JavaUUID.map(UserId.apply)

}
