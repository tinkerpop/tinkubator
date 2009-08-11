package org.linkedprocess.linkeddata.server.rewriter;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

/**
 * Author: josh
 * Date: Aug 11, 2009
 * Time: 3:32:06 PM
 */
public class RewriterSailConnection extends SailConnectionWrapper {
    private final RewritingSchema rewriters;
    private final ValueFactory valueFactory;

    public RewriterSailConnection(final SailConnection baseConnection,
                                  final RewritingSchema rewriters,
                                  final ValueFactory valueFactory) {
        super(baseConnection);
        this.rewriters = rewriters;
        this.valueFactory = valueFactory;
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj,
                                                                                URI pred,
                                                                                Value obj,
                                                                                final boolean includeInferred,
                                                                                Resource... contexts) throws SailException {
        if (subj instanceof URI) {
            subj = rewriters.getRewriter(RewritingSchema.PartOfSpeech.SUBJECT, RewritingSchema.Action.TO_STORE).rewrite((URI) subj);
        }
        pred = rewriters.getRewriter(RewritingSchema.PartOfSpeech.PREDICATE, RewritingSchema.Action.TO_STORE).rewrite(pred);
        if (obj instanceof URI) {
            obj = rewriters.getRewriter(RewritingSchema.PartOfSpeech.OBJECT, RewritingSchema.Action.TO_STORE).rewrite((URI) obj);
        }
        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] instanceof URI) {
                contexts[i] = rewriters.getRewriter(RewritingSchema.PartOfSpeech.CONTEXT, RewritingSchema.Action.TO_STORE).rewrite((URI) contexts[i]);                
            }
        }

        return new RewritingStatementIteration(this.getWrappedConnection().getStatements(subj, pred, obj, includeInferred, contexts));
    }

    private class RewritingStatementIteration implements CloseableIteration<Statement, SailException> {
        private final CloseableIteration<? extends Statement, SailException> baseIteration;

        public RewritingStatementIteration(final CloseableIteration<? extends Statement, SailException> baseIteration) {
            this.baseIteration = baseIteration;
        }

        public void close() throws SailException {
            baseIteration.close();
        }

        public boolean hasNext() throws SailException {
            return baseIteration.hasNext();
        }

        public Statement next() throws SailException {
            Statement st = baseIteration.next();

            Resource subject = st.getSubject();
            URI predicate = st.getPredicate();
            Value object = st.getObject();
            Resource context = st.getContext();

            if (subject instanceof URI) {
                subject = rewriters.getRewriter(
                        RewritingSchema.PartOfSpeech.SUBJECT, RewritingSchema.Action.FROM_STORE)
                        .rewrite((URI) subject);
            }
            predicate = rewriters.getRewriter(
                    RewritingSchema.PartOfSpeech.PREDICATE, RewritingSchema.Action.FROM_STORE)
                    .rewrite(predicate);
            if (object instanceof URI) {
                object = rewriters.getRewriter(
                        RewritingSchema.PartOfSpeech.OBJECT, RewritingSchema.Action.FROM_STORE)
                        .rewrite((URI) object);
            }
            if (null != context && context instanceof URI) {
                context = rewriters.getRewriter(
                        RewritingSchema.PartOfSpeech.CONTEXT, RewritingSchema.Action.FROM_STORE)
                        .rewrite((URI) context);
            }

            return valueFactory.createStatement(subject, predicate, object, context);
        }

        public void remove() throws SailException {
            baseIteration.remove();
        }
    }
}
