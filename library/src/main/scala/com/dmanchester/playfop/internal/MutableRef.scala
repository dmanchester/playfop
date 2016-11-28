package com.dmanchester.playfop.internal

/** A mutable reference. Aids in caching an expensive object.
  *  
  * ''Intended Usage:'' Client code instantiates a `MutableRef`. Then, whenever
  * that code requires the expensive object, it synchronizes on the `MutableRef`
  * and calls [[get]]. If `None` is returned, the client code--still under
  * synchronization--instantiates the object and calls [[set]].
  *
  * Instances of this class require external synchronization. They are not
  * thread-safe.
  */
class MutableRef[A] {

  private var item: Option[A] = None

  /** Gets the object ("`item`," to avoid using a reserved word).
    * 
    * @return `Some(item)`, where `item` was supplied during the most recent
    *         [[set]] call; or `None`, if [[set]] has never been called
    */
  def get: Option[A] = { item }

  /** Sets the object ("`item`").
    *
    * @param item the item to set
    */
  def set(item: A) = { this.item = Some(item) }
}
