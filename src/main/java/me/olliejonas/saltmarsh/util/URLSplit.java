package me.olliejonas.saltmarsh.util;

public record URLSplit(CharSequence url, String protocol, String subDomain, String domain, String path) {
}
