/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Marcus Stursberg
 *
 *  File :               CompilerException.java
 *  Description :        Indicate an error while compiling the installer.
 *  Author's email :     marcus@emsty.de
 *  Author's Website :   http://www.emasty.de
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.compiler;

/**
 * Indicates a Failure to compile.
 *
 * @author     Marcus Stursberg
 */
class CompilerException extends java.io.IOException
{
  /**
   * The throwable that caused this throwable to get thrown, or null if this
   * throwable was not caused by another throwable, or if the causative
   * throwable is unknown.  If this field is equal to this throwable itself,
   * it indicates that the cause of this throwable has not yet been
   * initialized.
   */
  private Throwable _cause = this;
  
  /**
   * Construct a new exception with the specified message.
   *
   * @param message Description of the error
   */
  public CompilerException(String message)
  {
    super(message);
  }
  
  /**
   * Construct a new exception with the specified message and wraps another
   * cause.
   *
   * @param message Description of the error
   * @param cause Throwable
   */
  public CompilerException(String message, Throwable cause)
  {
    super(message);
    this._cause = cause;
  }
  
  /**
   * Initializes the <i>cause</i> of this throwable to the specified value.
   * (The cause is the throwable that caused this throwable to get thrown.) 
   *
   * <p>This method can be called at most once.  It is generally called from 
   * within the constructor, or immediately after creating the
   * throwable.  If this throwable was created
   * with {@link Throwable(Throwable)} or
   * {@link Throwable(String,Throwable)}, this method cannot be called
   * even once.
   *
   * @param  cause the cause (which is saved for later retrieval by the
   *         {@link #getCause()} method).  (A <tt>null</tt> value is
   *         permitted, and indicates that the cause is nonexistent or
   *         unknown.)
   * @return  a reference to this <code>Throwable</code> instance.
   * @throws IllegalArgumentException if <code>cause</code> is this
   *         throwable.  (A throwable cannot be its own cause.)
   * @throws IllegalStateException if this throwable was
   *         created with {@link Throwable(Throwable)} or
   *         {@link Throwable(String,Throwable)}, or this method has already
   *         been called on this throwable.
   */
  public synchronized Throwable initCause(Throwable cause)
  {
    if (this._cause != this)
      throw new IllegalStateException("Can't overwrite cause");
    if (cause == this)
      throw new IllegalArgumentException("Self-causation not permitted");
    this._cause = cause;
    return this;
  }
  
  /**
   * Returns the cause of this throwable or <code>null</code> if the
   * cause is nonexistent or unknown.  (The cause is the throwable that
   * caused this throwable to get thrown.)
   *
   * <p>This implementation returns the cause that was supplied via one of
   * the constructors requiring a <tt>Throwable</tt>, or that was set after
   * creation with the {@link #initCause(Throwable)} method.  While it is
   * typically unnecessary to override this method, a subclass can override
   * it to return a cause set by some other means.  This is appropriate for
   * a "legacy chained throwable" that predates the addition of chained
   * exceptions to <tt>Throwable</tt>.  Note that it is <i>not</i>
   * necessary to override any of the <tt>PrintStackTrace</tt> methods,
   * all of which invoke the <tt>getCause</tt> method to determine the
   * cause of a throwable.
   *
   * @return  the cause of this throwable or <code>null</code> if the
   *          cause is nonexistent or unknown.
   */
  public Throwable getCause()
  {
    return (_cause==this ? null : _cause);
  }
}
