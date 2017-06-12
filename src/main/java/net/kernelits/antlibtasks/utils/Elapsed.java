package net.kernelits.antlibtasks.utils;

import java.util.concurrent.TimeUnit;

/**
 * Classe que permite calcular o tempo decorrido em um processo
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class Elapsed {

    // inicio do tempo
    private long start;

    private Elapsed() {
        reset();
    }

    // inicia o processo para contabilizar o tempo
    public static Elapsed init() {
        return new Elapsed();
    }

    // reinicia o tempo
    public Elapsed reset() {
        start = System.nanoTime();
        return this;
    }

    // retorna diferenca do inicio ate o momento da chamada deste metodo
    public long time() {
        long end = System.nanoTime();
        return end - start;
    }

    // retorna diferenca do inicio ate o momento da chamada deste metodo
    // com conversao de medida
    public long time(TimeUnit unit) {
        return unit.convert(time(), TimeUnit.NANOSECONDS);
    }

    // retorna diferenca do inicio ate o momento da chamada deste metodo
    // com formato em string
    public String toString() {

        // converte para milisegundos
        long milliseconds = time(TimeUnit.MILLISECONDS);

        // calculo para formatacao
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) (milliseconds / 60000) % 60;

        // realiza a formatacao
        return String.format("%d min, %d sec", minutes, seconds);
    }
}
