//
// Created by victor.paleologue on 02/11/20.
//

#ifndef FASTDOWNWARDPLANNER_DISPOSABLE_HPP
#define FASTDOWNWARDPLANNER_DISPOSABLE_HPP

#include <utility>

/**
 * An object which would run a function when disposed.
 * Destroying performs the disposal function.
 * Useful to ensure a function is called when leaving a scope.
 * @tparam T A function compatible with the void() signature.
 */
template<typename T>
struct Disposable {
    Disposable(T &&disposal_function) : _do_dispose(disposal_function), _was_disposed(false) {}

    ~Disposable() { dispose(); }

    void dispose() {
        if (!_was_disposed) {
            _do_dispose();
            _was_disposed = true;
        }
    }

private:
    T _do_dispose;
    bool _was_disposed;
};

template<typename T>
Disposable<T> make_disposable(T &&disposal_function) {
    return Disposable<T>(std::forward<T>(disposal_function));
}

#endif //FASTDOWNWARDPLANNER_DISPOSABLE_HPP
